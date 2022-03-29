package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.data.models.RecentBlockHashBody
import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.data.models.WalletApproval
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): List<WalletApproval?>
    suspend fun getRecentBlockHash(): RecentBlockHashResponse
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val solanaApiService: SolanaApiService
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): List<WalletApproval?> {
        return api.getWalletApprovals()
    }

    override suspend fun getRecentBlockHash(): RecentBlockHashResponse {
        return solanaApiService.recentBlockhash(RecentBlockHashBody())
    }
}
