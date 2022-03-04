package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.generateKeyPairDummyData
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.WalletSigners
import kotlin.random.Random

interface UserRepository {
    suspend fun authenticate(sessionToken: String): String
    suspend fun retrieveSessionToken(username: String, password: String): String
    suspend fun verifyUser(): VerifyUser
    suspend fun getWalletSigners(): WalletSigners
    suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner
    suspend fun generateKeyPair(): Pair<String, String>
    suspend fun generateRandomPassword(): String
    suspend fun saveRandomPasswordToCloud(randomPassword: String)
}

class UserRepositoryImpl(
    val authProvider: AuthProvider,
    val api: BrooklynApiService
) : UserRepository {
    override suspend fun retrieveSessionToken(username: String, password: String): String =
        authProvider.getSessionToken(username, password)

    override suspend fun authenticate(sessionToken: String): String =
        authProvider.authenticate(sessionToken)

    override suspend fun verifyUser(): VerifyUser = api.verifyUser()

    override suspend fun getWalletSigners(): WalletSigners = api.walletSigners()

    override suspend fun addWalletSigner(walletSignerBody: WalletSigner): WalletSigner =
        api.addWalletSigner(walletSignerBody = walletSignerBody)

    override suspend fun generateKeyPair(): Pair<String, String> = generateKeyPairDummyData()

    override suspend fun generateRandomPassword(): String =
        Random.nextInt(from = 1000, until = 9999).toString()

    override suspend fun saveRandomPasswordToCloud(randomPassword: String) {}
}
