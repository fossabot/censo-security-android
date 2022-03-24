package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.generateWalletApprovalsDummyData
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.WalletApprovals
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): WalletApprovals
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): WalletApprovals {
        delay(3000)
        return WalletApprovals(
            listOf(
                generateWalletApprovalsDummyData(),
                generateWalletApprovalsDummyData(),
                generateWalletApprovalsDummyData()
            )
        )
        //api.getWalletApprovals()
    }

}
