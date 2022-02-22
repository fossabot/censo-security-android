package com.strikeprotocols.mobile.domain.use_case

import com.okta.authn.sdk.AuthenticationFailureException
import com.okta.authn.sdk.client.AuthenticationClients
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.domain.use_case.SignInUseCase.Companion.AUTH_FAILED
import com.strikeprotocols.mobile.domain.use_case.SignInUseCase.Companion.SUCCESS_STATUS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    fun execute(username: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val authenticationClient = AuthenticationClients.builder()
                .setOrgUrl(BuildConfig.OKTA_BASE_URL)
                .build()

            authenticationClient.authenticate(
                username, password.toCharArray(), null, null
            )?.run {
                if (statusString == SUCCESS_STATUS) {
                    val fullAuthResult = userRepository.authenticate(sessionToken = sessionToken)
                    emit(Resource.Success(fullAuthResult))
                } else {
                    emit(Resource.Error(AUTH_FAILED))
                }
            }

        } catch (e: AuthenticationFailureException) {
            emit(Resource.Error(AUTH_FAILED))
        }
    }

    object Companion {
        const val SUCCESS_STATUS = "SUCCESS"
        const val AUTH_FAILED = "AUTH_FAILED"
    }

}