package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeError
import com.strikeprotocols.mobile.data.models.InitiationDisposition
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getWalletApprovals(): Resource<List<WalletApproval?>>
    suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): Resource<ApprovalDispositionRequest.RegisterApprovalDispositionBody>
    suspend fun approveOrDenyInitiation(
        requestId: String,
        initialDisposition: InitiationDisposition
    ) : Resource<InitiationRequest.InitiateRequestBody>
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val userRepository: UserRepository
) : ApprovalsRepository, BaseRepository() {

    override suspend fun getWalletApprovals(): Resource<List<WalletApproval?>> =
        retrieveApiResource { api.getWalletApprovals() }

    override suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition
    ): Resource<ApprovalDispositionRequest.RegisterApprovalDispositionBody> {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (registerApprovalDisposition.anyItemNull()) {
            return Resource.Error(strikeError = StrikeError.DefaultDispositionError())
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            return Resource.Error(strikeError = StrikeError.MissingUserEmailError())
        }

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = requestId,
            approvalDisposition = registerApprovalDisposition.approvalDisposition!!,
            nonces = registerApprovalDisposition.nonces!!,
            email = userEmail,
            requestType = registerApprovalDisposition.solanaApprovalRequestType!!
        )

        val registerApprovalDispositionBody = try {
                approvalDispositionRequest.convertToApiBody(encryptionManager)
        } catch (e: Exception) {
            return Resource.Error(strikeError = StrikeError.SigningDataError())
        }

        return retrieveApiResource {
            api.approveOrDenyDisposition(
                requestId = approvalDispositionRequest.requestId,
                registerApprovalDispositionBody = registerApprovalDispositionBody
            )
        }
    }

    override suspend fun approveOrDenyInitiation(
        requestId: String,
        initialDisposition: InitiationDisposition
    ) : Resource<InitiationRequest.InitiateRequestBody> {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (initialDisposition.anyItemNull()) {
            return Resource.Error(strikeError = StrikeError.DefaultDispositionError())
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            return Resource.Error(strikeError = StrikeError.MissingUserEmailError())
        }

        val initiationRequest = InitiationRequest(
            requestId = requestId,
            approvalDisposition = initialDisposition.approvalDisposition!!,
            initiation = initialDisposition.multiSigOpInitiationDetails!!.multisigOpInitiation,
            requestType = initialDisposition.multiSigOpInitiationDetails.requestType,
            nonces = initialDisposition.nonces!!,
            email = userEmail
        )
        val initiationRequestBody = try {
            initiationRequest.convertToApiBody(encryptionManager)
        } catch (e: Exception) {
            return Resource.Error(strikeError = StrikeError.SigningDataError())
        }

        return retrieveApiResource {
            api.approveOrDenyInitiation(
                requestId = requestId,
                initiationRequestBody = initiationRequestBody
            )
        }
    }
}
