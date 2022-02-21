package com.strikeprotocols.mobile.domain.use_case

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.domain.use_case.SignInUseCase.Companion.AUTH_FAILED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    fun execute(username: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        val sessionToken = userRepository.retrieveSessionToken(username, password)
        val token = userRepository.authenticate(sessionToken)
        emit(Resource.Success(token))
    }

    object Companion {
        const val SUCCESS_STATUS = "SUCCESS"
        const val AUTH_FAILED = "AUTH_FAILED"
    }
}

class SessionTokenException : Exception(AUTH_FAILED)