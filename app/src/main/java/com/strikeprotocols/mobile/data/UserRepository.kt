package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.WalletSigners
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.lang.Exception
import java.util.*

interface UserRepository {
    suspend fun authenticate(sessionToken: String): String
    suspend fun retrieveSessionToken(username: String, password: String): String
    suspend fun verifyUser(): VerifyUser
    suspend fun getWalletSigners(): WalletSigners
    suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner
    suspend fun generateInitialAuthData(): WalletSigner
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

    override suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner =
        api.addWalletSigner(walletSignerBody = walletSignerBody)

    //todo: add exception logic in here
    // str-68: https://linear.app/strike-android/issue/STR-68/add-exception-logic-to-initial-auth-data-in-userrepository
    override suspend fun generateInitialAuthData() : WalletSigner {
        val keyPair = encryptionManager.createKeyPair()
        val generatedPassword = encryptionManager.generatePassword()

        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = keyPair.public as Ed25519PublicKeyParameters

        securePreferences.saveGeneratedPassword(generatedPassword)

        val encryptedPrivateKey =
            encryptionManager.encrypt(
                message = BaseWrapper.encode(privateKey.encoded),
                generatedPassword = generatedPassword
            )

        return WalletSigner(
            encryptedKey = encryptedPrivateKey,
            publicKey = BaseWrapper.encode(publicKey.encoded),
            walletType = WalletSigner.WALLET_TYPE_SOLANA
        )
    }
 }
