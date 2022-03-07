package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.WalletSigners
import kotlinx.coroutines.delay

interface UserRepository {
    suspend fun authenticate(sessionToken: String): String
    suspend fun retrieveSessionToken(username: String, password: String): String
    suspend fun verifyUser(): VerifyUser
    suspend fun getWalletSigners(): WalletSigners
    suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner
    suspend fun generateInitialAuthData(): InitialAuthData
    suspend fun getSavedPassword(): String
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

    override suspend fun verifyUser(): VerifyUser = api.verifyUser()

    override suspend fun getWalletSigners(): WalletSigners = api.walletSigners()

    override suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner {
        delay(3000)
        return WalletSigner(encryptedKey = "", publicKey = "", walletType = "")
        //api.addWalletSigner(walletSignerBody = walletSignerBody)
    }

    override suspend fun getSavedPassword(): String = securePreferences.retrieveGeneratedPassword()

    //todo: add exception logic in here
    // str-68: https://linear.app/strike-android/issue/STR-68/add-exception-logic-to-initial-auth-data-in-userrepository
    override suspend fun generateInitialAuthData() : InitialAuthData {
        val keyPair = encryptionManager.createKeyPair()
        val generatedPassword = encryptionManager.generatePassword()

        securePreferences.saveGeneratedPassword(generatedPassword)

        val encryptedPrivateKey =
            encryptionManager.encrypt(
                message = BaseWrapper.encode(keyPair.privateKey),
                generatedPassword = generatedPassword
            )

        return InitialAuthData(
            walletSignerBody = WalletSigner(
                encryptedKey = encryptedPrivateKey,
                publicKey = BaseWrapper.encode(keyPair.publicKey),
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            ),
            generatedPassword = generatedPassword
        )
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
