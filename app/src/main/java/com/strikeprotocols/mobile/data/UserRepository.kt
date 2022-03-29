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
    suspend fun generateInitialAuthData(): InitialAuthData
    suspend fun saveGeneratedPassword(generatedPassword: ByteArray)
    suspend fun getSavedPassword(): String
    suspend fun clearSavedPassword()
    suspend fun userLoggedIn(): Boolean
    suspend fun setUserLoggedIn()
    suspend fun logOut() : Boolean
    suspend fun clearGeneratedAuthData()
    suspend fun regenerateDataAndUploadToBackend(): WalletSigner
    suspend fun retrieveUserEmail(): String
    suspend fun saveUserEmail(email: String)
    suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean
}

class UserRepositoryImpl(
    private val authProvider: AuthProvider,
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val securePreferences: SecurePreferences

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

    override suspend fun saveGeneratedPassword(generatedPassword: ByteArray) {
        val userEmail = retrieveUserEmail()
        if (userEmail.isNotEmpty()) {
            securePreferences.saveGeneratedPassword(
                email = userEmail, generatedPassword = generatedPassword
            )
        }
    }

    override suspend fun getSavedPassword(): String {
        val userEmail = retrieveUserEmail()
        return securePreferences.retrieveGeneratedPassword(email = userEmail)
    }

    //todo: add exception logic in here
    // str-68: https://linear.app/strike-android/issue/STR-68/add-exception-logic-to-initial-auth-data-in-userrepository
    override suspend fun generateInitialAuthData(): InitialAuthData {
        val userEmail = retrieveUserEmail()

        val keyPair = encryptionManager.createKeyPair()
        val generatedPassword = encryptionManager.generatePassword()

        securePreferences.saveGeneratedPassword(
            email = userEmail, generatedPassword = generatedPassword)

        val encryptedPrivateKey =
            encryptionManager.encrypt(
                message = BaseWrapper.encode(keyPair.privateKey),
                generatedPassword = generatedPassword
            )

        securePreferences.savePrivateKey(email = userEmail, privateKey = encryptedPrivateKey)

        return InitialAuthData(
            walletSignerBody = WalletSigner(
                encryptedKey = encryptedPrivateKey,
                publicKey = BaseWrapper.encode(keyPair.publicKey),
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            ),
            generatedPassword = generatedPassword
        )
    }

    override suspend fun clearGeneratedAuthData() {
        val userEmail = retrieveUserEmail()
        saveUserEmail("")
        securePreferences.clearPrivateKey(email = userEmail)
        securePreferences.clearSavedPassword(email = userEmail)
    }

    override suspend fun regenerateDataAndUploadToBackend() : WalletSigner {
        val userEmail = retrieveUserEmail()
        val encryptedKey = securePreferences.retrievePrivateKey(userEmail)
        val decryptionKey = securePreferences.retrieveGeneratedPassword(userEmail)

        val publicKey = encryptionManager.regeneratePublicKey(
            encryptedPrivateKey = encryptedKey,
            decryptionKey = decryptionKey
        )

        val walletSigner = WalletSigner(
            publicKey = publicKey,
            encryptedKey = encryptedKey,
            walletType = WALLET_TYPE_SOLANA
        )

        return walletSigner

        //api.addWalletSigner(walletSigner)
    }

    override suspend fun retrieveUserEmail(): String {
        val email = SharedPrefsHelper.retrieveUserEmail()

        if(email.isNotEmpty()) return email

        return try {
            val oktaEmail = authProvider.getUserEmail()
            ""
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun saveUserEmail(email: String) {
        SharedPrefsHelper.saveUserEmail(email)
    }

    override suspend fun clearSavedPassword() {
        val userEmail = retrieveUserEmail()
        if(userEmail.isNotEmpty()) {
            securePreferences.clearSavedPassword(email = userEmail)
        }
    }

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

    override suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean {
        val userEmail = retrieveUserEmail()
        val generatedPassword = securePreferences.retrieveGeneratedPassword(email = userEmail)

        if (generatedPassword.isEmpty()) {
            return false
        }

        val publicKey = verifyUser.publicKeys?.firstOrNull { !it?.key.isNullOrEmpty() }?.key

        if (publicKey.isNullOrEmpty()) {
            return false
        }

        //api call to get wallet signer
        for (walletSigner in walletSigners) {
            if (walletSigner?.publicKey != null && walletSigner.publicKey == publicKey) {
                val validPair = encryptionManager.verifyKeyPair(
                    encryptedPrivateKey = walletSigner.encryptedKey,
                    publicKey = walletSigner.publicKey,
                    symmetricKey = generatedPassword
                )

                if (validPair) {
                    walletSigner.encryptedKey?.let { encryptedKey ->
                        securePreferences.savePrivateKey(
                            email = userEmail, privateKey = encryptedKey
                        )
                    }
                    return true
                }
            }
        }

        return false
    }
}

data class InitialAuthData(val walletSignerBody: WalletSigner, val generatedPassword: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InitialAuthData

        if (walletSignerBody != other.walletSignerBody) return false
        if (!generatedPassword.contentEquals(other.generatedPassword)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = walletSignerBody.hashCode()
        result = 31 * result + generatedPassword.contentHashCode()
        return result
    }
}

enum class UserAuthFlow {
    FIRST_LOGIN, LOCAL_KEY_PRESENT_NO_BACKEND_KEYS, KEY_VALIDATED,
    EXISTING_BACKEND_KEY_LOCAL_KEY_MISSING, NO_LOCAL_KEY_AVAILABLE, NO_VALID_KEY
}