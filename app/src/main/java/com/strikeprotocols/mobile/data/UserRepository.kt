package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.data.models.VerifyUser

interface UserRepository {
    suspend fun authenticate(sessionToken: String): String
    suspend fun retrieveSessionToken(username: String, password: String): String
    suspend fun verifyUser(): VerifyUser
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
}
