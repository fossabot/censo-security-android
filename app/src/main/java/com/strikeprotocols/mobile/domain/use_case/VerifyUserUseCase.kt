package com.strikeprotocols.mobile.domain.use_case

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.VerifyUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VerifyUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun execute(): Flow<Resource<VerifyUser>> = flow {
        try {
            emit(Resource.Loading())
            val verifyUserResponse = userRepository.verifyUser()
            emit(Resource.Success(verifyUserResponse))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}