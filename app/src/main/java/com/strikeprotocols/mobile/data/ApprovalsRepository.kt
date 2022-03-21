package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.WalletApprovals
import okhttp3.ResponseBody
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): WalletApprovals
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): WalletApprovals {
        return api.getWalletApprovals()
    }

}
