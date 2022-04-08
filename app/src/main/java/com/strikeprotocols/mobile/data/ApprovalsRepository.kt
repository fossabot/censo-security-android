package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): List<WalletApproval?>
    suspend fun approveOrDenyDisposition(
        registerApprovalDisposition: RegisterApprovalDisposition
    ): ApprovalDispositionRequest.RegisterApprovalDispositionBody
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val userRepository: UserRepository
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): List<WalletApproval?> {
        return api.getWalletApprovals()
    }

    override suspend fun approveOrDenyDisposition(
        registerApprovalDisposition: RegisterApprovalDisposition
    ): ApprovalDispositionRequest.RegisterApprovalDispositionBody {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (registerApprovalDisposition.anyItemNull()) {
            throw Exception(registerApprovalDisposition.getError().error)
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            throw Exception("MISSING USER EMAIL")
        }

        val approvalDispositionRequest = ApprovalDispositionRequest(
            approvalDisposition = registerApprovalDisposition.approvalDisposition!!,
            blockhash = registerApprovalDisposition.recentBlockhash!!,
            email = userEmail,
            requestType = registerApprovalDisposition.solanaApprovalRequestType!!
        )

        val registerApprovalDispositionBody =
            approvalDispositionRequest.convertToApiBody(encryptionManager)

        return api.approveOrDenyDisposition(
            requestId = approvalDispositionRequest.requestId,
            registerApprovalDispositionBody = registerApprovalDispositionBody
        )
    }
}
