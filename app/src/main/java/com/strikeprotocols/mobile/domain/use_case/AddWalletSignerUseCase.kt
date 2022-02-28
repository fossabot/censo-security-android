package com.strikeprotocols.mobile.domain.use_case

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.WalletSigner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AddWalletSignerUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun execute(walletSignerBody: WalletSigner): Flow<Resource<WalletSigner>> = flow {
        try {
            emit(Resource.Loading())
            val walletSignerResponse =
                userRepository.addWalletSigner(walletSignerBody = walletSignerBody)
            emit(Resource.Success(walletSignerResponse))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}