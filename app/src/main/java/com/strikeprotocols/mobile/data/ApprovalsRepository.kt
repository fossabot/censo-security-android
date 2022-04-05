package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.ApprovalsRepositoryImpl.RegisterApprovalDispositionBody
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): List<WalletApproval?>
    suspend fun approveOrDenyDisposition(
        requestId: String?,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): RegisterApprovalDispositionBody
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
        requestId: String?,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): RegisterApprovalDispositionBody {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (registerApprovalDisposition.anyItemNull()) {
            throw Exception(registerApprovalDisposition.getError().error)
        }

        val userEmail = userRepository.retrieveUserEmail()

        val signature = try {
            encryptionManager.signApprovalDispositionMessage(
                signable = registerApprovalDisposition.signable!!,
                userEmail = userEmail
            )
        } catch (e: Exception) {
            throw Exception(ApprovalDispositionError.SIGNING_DATA_FAILURE.error)
        }

        val registerApprovalDispositionBody = RegisterApprovalDispositionBody(
            approvalDisposition = registerApprovalDisposition.approvalDisposition!!,
            recentBlockHash = registerApprovalDisposition.recentBlockhash!!,
            signature = signature
        )

        return api.approveOrDenyDisposition(
            requestId = requestId ?: "",
            registerApprovalDispositionBody = registerApprovalDispositionBody
        )
    }

    inner class RegisterApprovalDispositionBody(
        val approvalDisposition: ApprovalDisposition,
        val recentBlockHash: String,
        val signature: String
    )
}
