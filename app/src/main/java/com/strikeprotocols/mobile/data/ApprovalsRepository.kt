package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.MockedApprovals
import com.strikeprotocols.mobile.data.models.InitiationDisposition
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalConstants.MISSING_USER_EMAIL
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): List<WalletApproval?>
    suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): ApprovalDispositionRequest.RegisterApprovalDispositionBody
    suspend fun approveOrDenyInitiation(
        requestId: String,
        initialDisposition: InitiationDisposition
    ) : InitiationRequest.InitiateRequestBody
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val userRepository: UserRepository
) : ApprovalsRepository {

    override suspend fun getWalletApprovals(): List<WalletApproval?> {
        return MockedApprovals.get16StandardApprovals()
    }

    override suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): ApprovalDispositionRequest.RegisterApprovalDispositionBody {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (registerApprovalDisposition.anyItemNull()) {
            throw Exception(registerApprovalDisposition.getError().error)
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            throw Exception(MISSING_USER_EMAIL)
        }

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = requestId,
            approvalDisposition = registerApprovalDisposition.approvalDisposition!!,
            nonces = registerApprovalDisposition.nonces!!,
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

    override suspend fun approveOrDenyInitiation(
        requestId: String,
        initialDisposition: InitiationDisposition
    ) : InitiationRequest.InitiateRequestBody {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (initialDisposition.anyItemNull()) {
            throw Exception(initialDisposition.getError().error)
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            throw Exception(MISSING_USER_EMAIL)
        }

        val initiationRequest = InitiationRequest(
            requestId = requestId,
            approvalDisposition = initialDisposition.approvalDisposition!!,
            nonces = initialDisposition.nonces!!,
            email = userEmail,
            initiation = initialDisposition.multiSigOpInitiationDetails!!.multisigOpInitiation,
            requestType = initialDisposition.multiSigOpInitiationDetails.requestType
        )

        val initiationRequestBody =
            initiationRequest.convertToApiBody(encryptionManager)

        return api.approveOrDenyInitiation(
            requestId = requestId,
            initiationRequestBody = initiationRequestBody
        )
    }
}
