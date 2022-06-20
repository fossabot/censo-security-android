package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.WalletSigner.Companion.WALLET_TYPE_SOLANA

interface UserRepository {
    suspend fun authenticate(sessionToken: String): String
    suspend fun retrieveSessionToken(username: String, password: String): String
    suspend fun verifyUser(): VerifyUser
    suspend fun getWalletSigners(): List<WalletSigner?>
    suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner
    suspend fun generateInitialAuthDataAndSaveKeyToUser(phrase: String): InitialAuthData
    suspend fun regenerateAuthDataAndSaveKeyToUser(phrase: String, backendPublicKey: String)
    suspend fun userLoggedIn(): Boolean
    suspend fun setUserLoggedIn()
    suspend fun logOut() : Boolean
    suspend fun getSavedPrivateKey(): String
    suspend fun clearGeneratedAuthData()
    suspend fun regenerateDataAndUploadToBackend(): WalletSigner
    suspend fun retrieveUserEmail(): String
    suspend fun saveUserEmail(email: String)
    suspend fun generatePhrase() : String
    suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean
}

class UserRepositoryImpl(
    private val authProvider: AuthProvider,
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val securePreferences: SecurePreferences,
    private val phraseValidator: PhraseValidator
) : UserRepository {
    override suspend fun retrieveSessionToken(username: String, password: String): String =
        authProvider.getSessionToken(username, password)

    override suspend fun authenticate(sessionToken: String): String =
        authProvider.authenticate(sessionToken)

    override suspend fun verifyUser(): VerifyUser {
        return api.verifyUser()
    }

    override suspend fun getWalletSigners(): List<WalletSigner?> {
        return api.walletSigners()
    }

    override suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner {
        return api.addWalletSigner(walletSignerBody = walletSignerBody)
    }

    override suspend fun generateInitialAuthDataAndSaveKeyToUser(phrase: String): InitialAuthData {
        val userEmail = retrieveUserEmail()
        val keyPair = encryptionManager.createKeyPair(phrase)

        securePreferences.savePrivateKey(
            email = userEmail, privateKey = keyPair.privateKey)

        return InitialAuthData(
            walletSignerBody = WalletSigner(
                publicKey = BaseWrapper.encode(keyPair.publicKey),
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            )
        )
    }

    override suspend fun regenerateAuthDataAndSaveKeyToUser(phrase: String, backendPublicKey: String) {
        val userEmail = retrieveUserEmail()
        //Validate the phrase firsts
        if (!phraseValidator.isPhraseValid(phrase)) {
            throw Exception("Invalid Phrase")
        }

        //Regenerate the key pair
        val keyPair = encryptionManager.createKeyPair(phrase)

        //Verify the keyPair
        val validPair = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
        )
        if (!validPair) {
            throw Exception("Invalid Regenerated KeyPair")
        }

        //Verify the backend public key and recreated private key work together
        val phraseKeyMatchesBackendKey = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = backendPublicKey
        )
        if (!phraseKeyMatchesBackendKey) {
            throw Exception("Phrase Key Does Not Match Backend Key")
        }

        //Save the recreated private key if the pair is valid together
        securePreferences.savePrivateKey(
            email = userEmail, privateKey = keyPair.privateKey
        )
    }

    override suspend fun clearGeneratedAuthData() {
        val userEmail = retrieveUserEmail()
        saveUserEmail("")
        securePreferences.clearPrivateKey(email = userEmail)
    }

    override suspend fun regenerateDataAndUploadToBackend() : WalletSigner {
        val userEmail = retrieveUserEmail()
        val mainKey = securePreferences.retrievePrivateKey(userEmail)
        val publicKey = encryptionManager.regeneratePublicKey(mainKey = mainKey)

        val walletSigner = WalletSigner(
            publicKey = publicKey,
            walletType = WALLET_TYPE_SOLANA
        )

        return api.addWalletSigner(walletSigner)
    }

    override suspend fun retrieveUserEmail(): String {
        return try {
            authProvider.retrieveUserEmail()
        } catch (e: Exception) {
            ""
        }
    }

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
        return securePreferences.retrievePrivateKey(email = userEmail)
    }

    override suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean {
        val userEmail = retrieveUserEmail()
        val privateKey = securePreferences.retrievePrivateKey(email = userEmail)

        if (privateKey.isEmpty()) {
            return false
        }

        val publicKey = verifyUser.firstPublicKey

        if (publicKey.isNullOrEmpty()) {
            return false
        }

        //api call to get wallet signer
        for (walletSigner in walletSigners) {
            if (walletSigner?.publicKey != null && walletSigner.publicKey == publicKey) {
                val validPair = encryptionManager.verifyKeyPair(
                    privateKey = privateKey,
                    publicKey = walletSigner.publicKey,
                )

                if (validPair) {
                    return true
                }
            }
        }

        return false
    }
}

data class InitialAuthData(val walletSignerBody: WalletSigner)

enum class UserAuthFlow {
    FIRST_LOGIN, LOCAL_KEY_PRESENT_NO_BACKEND_KEYS, KEY_VALIDATED,
    EXISTING_BACKEND_KEY_LOCAL_KEY_MISSING, NO_LOCAL_KEY_AVAILABLE, NO_VALID_KEY
}