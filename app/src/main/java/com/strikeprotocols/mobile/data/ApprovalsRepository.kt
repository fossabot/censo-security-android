package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.generateRecentBlockhashDummyData
import com.strikeprotocols.mobile.data.models.RecentBlockHashBody
import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.data.models.WalletApproval
import kotlinx.coroutines.delay
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): List<WalletApproval?>
    suspend fun getRecentBlockHash(): String//RecentBlockHashResponse
    suspend fun registerApprovalDisposition(): Boolean
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val solanaApiService: SolanaApiService
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): List<WalletApproval?> {
        return api.getWalletApprovals()
    }

    override suspend fun getRecentBlockHash(): String {
        delay(3000)
        return generateRecentBlockhashDummyData()//solanaApiService.recentBlockhash(RecentBlockHashBody())
    }

    override suspend fun registerApprovalDisposition(): Boolean {
        delay(3000)
        return true
    }
}
