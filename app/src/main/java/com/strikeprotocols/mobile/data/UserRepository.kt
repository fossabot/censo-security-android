package com.strikeprotocols.mobile.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.AuthDataException.*
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.WalletSigner.Companion.WALLET_TYPE_SOLANA

interface UserRepository {
    suspend fun login(email: String, password: String): Resource<LoginResponse>
    suspend fun saveToken(token: String)
    suspend fun verifyUser(): Resource<VerifyUser>
    suspend fun getWalletSigners(): Resource<List<WalletSigner?>>
    suspend fun addWalletSigner(walletSignerBody: WalletSigner):Resource<WalletSigner>
    suspend fun generateInitialAuthDataAndSaveKeyToUser(mnemonic: Mnemonics.MnemonicCode): WalletSigner
    suspend fun regenerateAuthDataAndSaveKeyToUser(phrase: String, backendPublicKey: String)
    suspend fun userLoggedIn(): Boolean
    suspend fun setUserLoggedIn()
    suspend fun logOut() : Boolean
    suspend fun getSavedPrivateKey(): String
    suspend fun regenerateDataAndUploadToBackend(): Resource<WalletSigner>
    suspend fun retrieveUserEmail(): String
    fun retrieveCachedUserEmail(): String
    suspend fun saveUserEmail(email: String)
    suspend fun generatePhrase() : String
    suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean
    suspend fun checkMinimumVersion() : Resource<SemanticVersionResponse>
}

class UserRepositoryImpl(
    private val authProvider: AuthProvider,
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val securePreferences: SecurePreferences,
    private val phraseValidator: PhraseValidator,
    private val versionApiService: SemVersionApiService,
    private val applicationContext: Context
) : UserRepository, BaseRepository() {

    @SuppressLint("HardwareIds")
    override suspend fun login(email: String, password: String): Resource<LoginResponse> {
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )

        val loginBody = LoginBody(
            deviceId = deviceId,
            credentials = LoginCredentials(
                type = LoginType.PASSWORD_BASED,
                email = email,
                password = password
            )
        )

        return retrieveApiResource { api.login(loginBody = loginBody) }
    }

    override suspend fun saveToken(token: String) {
        securePreferences.saveToken(token = token)
    }

    override suspend fun verifyUser(): Resource<VerifyUser> =
        retrieveApiResource { api.verifyUser() }

    override suspend fun getWalletSigners(): Resource<List<WalletSigner?>> =
        retrieveApiResource { api.walletSigners() }

    override suspend fun addWalletSigner(walletSignerBody: WalletSigner): Resource<WalletSigner> =
        retrieveApiResource { api.addWalletSigner(walletSignerBody = walletSignerBody) }

    override suspend fun generateInitialAuthDataAndSaveKeyToUser(mnemonic: Mnemonics.MnemonicCode): WalletSigner {
        val userEmail = retrieveUserEmail()
        val keyPair = encryptionManager.createKeyPair(mnemonic)

        //Verify the keyPair
        val validPair = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
        )
        if (!validPair) {
            throw InvalidKeyPairException()
        }

        securePreferences.saveRootSeed(
            email = userEmail, rootSeed = mnemonic.toSeed()
        )
        securePreferences.saveSolanaKey(
            email = userEmail, privateKey = keyPair.privateKey)

        return WalletSigner(
            publicKey = BaseWrapper.encode(keyPair.publicKey),
            walletType = WALLET_TYPE_SOLANA
        )

    }

    override suspend fun regenerateAuthDataAndSaveKeyToUser(phrase: String, backendPublicKey: String) {
        val userEmail = retrieveUserEmail()
        //Validate the phrase firsts
        if (!phraseValidator.isPhraseValid(phrase)) {
            throw InvalidPhraseException()
        }

        //Regenerate the key pair
        val keyPair = encryptionManager.createKeyPair(Mnemonics.MnemonicCode(phrase))

        //Verify the keyPair
        val validPair = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
        )
        if (!validPair) {
            throw InvalidKeyPairException()
        }

        //Verify the backend public key and recreated private key work together
        val phraseKeyMatchesBackendKey = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = backendPublicKey
        )
        if (!phraseKeyMatchesBackendKey) {
            throw PhraseKeyDoesNotMatchBackendKeyException()
        }

        //Save the recreated private key if the pair is valid together
        securePreferences.saveRootSeed(
            email = userEmail,
            Mnemonics.MnemonicCode(phrase = phrase).toSeed()
        )
        securePreferences.saveSolanaKey(
            email = userEmail, privateKey = keyPair.privateKey
        )
    }

    override suspend fun regenerateDataAndUploadToBackend() : Resource<WalletSigner> {
        return try {
            val userEmail = retrieveUserEmail()
            val solanaKey = securePreferences.retrieveSolanaKey(userEmail)
            val publicKey = encryptionManager.regeneratePublicKey(privateKey = solanaKey)

            val walletSigner = WalletSigner(
                publicKey = publicKey,
                walletType = WALLET_TYPE_SOLANA
            )

            retrieveApiResource { api.addWalletSigner(walletSigner) }
        } catch (e: Exception) {
            Resource.Error()
        }
    }

    override suspend fun retrieveUserEmail(): String {
        return try {
            authProvider.retrieveUserEmail()
        } catch (e: Exception) {
            ""
        }
    }

    override fun retrieveCachedUserEmail() = SharedPrefsHelper.retrieveUserEmail()

    override suspend fun saveUserEmail(email: String) {
        SharedPrefsHelper.saveUserEmail(email)
    }

    override suspend fun generatePhrase(): String = encryptionManager.generatePhrase()

    override suspend fun userLoggedIn() = SharedPrefsHelper.isUserLoggedIn()
    override suspend fun setUserLoggedIn() = SharedPrefsHelper.setUserLoggedIn(true)

    override suspend fun logOut(): Boolean {
        return try {
            authProvider.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSavedPrivateKey(): String {
        val userEmail = retrieveUserEmail()
        return securePreferences.retrieveSolanaKey(email = userEmail)
    }

    override suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean {
        val userEmail = retrieveUserEmail()
        val privateKey = securePreferences.retrieveSolanaKey(email = userEmail)

        if (privateKey.isEmpty()) {
            return false
        }

        val publicKey = verifyUser.firstPublicKey()

        if (publicKey.isNullOrEmpty()) {
            return false
        }

        //api call to get wallet signer
        for (walletSigner in walletSigners) {
            if (walletSigner?.publicKey != null && walletSigner.publicKey == publicKey) {
                val validPair =
                    try {
                        encryptionManager.verifyKeyPair(
                            privateKey = privateKey,
                            publicKey = walletSigner.publicKey,
                        )
                    } catch (e: Exception) {
                        false
                    }

                if (validPair) {
                    return true
                }
            }
        }

        return false
    }

    override suspend fun checkMinimumVersion(): Resource<SemanticVersionResponse> {
        return retrieveApiResource {
            versionApiService.getMinimumVersion()
        }
    }
}