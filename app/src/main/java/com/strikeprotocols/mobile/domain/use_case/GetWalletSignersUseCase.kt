package com.strikeprotocols.mobile.domain.use_case

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.WalletSigners
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetWalletSignersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun execute(): Flow<Resource<WalletSigners>> = flow {
        try {
            emit(Resource.Loading())
            val walletSignersResponse = userRepository.getWalletSigners()
            emit(Resource.Success(walletSignersResponse))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}