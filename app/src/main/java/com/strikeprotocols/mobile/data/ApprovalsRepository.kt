package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.data.models.WalletApproval
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): List<WalletApproval?>
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): List<WalletApproval?> {
        return api.getWalletApprovals()
    }

}
