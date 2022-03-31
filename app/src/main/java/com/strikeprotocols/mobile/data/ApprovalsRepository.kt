package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.generateWalletApprovalsDummyData
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.RecentBlockHashBody
import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.WalletApproval
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): List<WalletApproval?>
    suspend fun getRecentBlockHash(): RecentBlockHashResponse
    suspend fun approveOrDenyDisposition(
        requestId: String?,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): RegisterApprovalDisposition
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val solanaApiService: SolanaApiService
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): List<WalletApproval?> {
        return listOf(
            generateWalletApprovalsDummyData()
        )
        //return api.getWalletApprovals()
    }

    override suspend fun getRecentBlockHash(): RecentBlockHashResponse {
        return solanaApiService.recentBlockhash(RecentBlockHashBody())
    }

    override suspend fun approveOrDenyDisposition(
        requestId: String?,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): RegisterApprovalDisposition {
        return registerApprovalDisposition
//        return api.approveOrDenyDisposition(
//            requestId = requestId ?: "",
//            registerApprovalDisposition = registerApprovalDisposition
//        )
    }
}
