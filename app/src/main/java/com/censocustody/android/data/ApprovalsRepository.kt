package com.censocustody.android.data

import androidx.biometric.BiometricPrompt.CryptoObject
import com.censocustody.android.common.Resource
import com.censocustody.android.common.CensoError
import com.censocustody.android.data.models.InitiationDisposition
import com.censocustody.android.data.models.RegisterApprovalDisposition
import com.censocustody.android.data.models.approval.ApprovalDispositionRequest
import com.censocustody.android.data.models.approval.InitiationRequest
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getApprovalRequests(): Resource<List<ApprovalRequestV2?>>
    suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition,
        cryptoObject: CryptoObject
    ): Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body>
    suspend fun approveOrDenyInitiation(
        requestId: String,
        initialDisposition: InitiationDisposition,
        cryptoObject: CryptoObject
    ) : Resource<InitiationRequest.InitiateRequestBody>
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val userRepository: UserRepository
) : ApprovalsRepository, BaseRepository() {

    override suspend fun getApprovalRequests(): Resource<List<ApprovalRequestV2?>> =
        retrieveApiResource { api.getApprovalRequests() }

    override suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition,
        cryptoObject: CryptoObject,
    ): Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body> {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (registerApprovalDisposition.anyItemNull()) {
            return Resource.Error(censoError = CensoError.DefaultDispositionError())
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            return Resource.Error(censoError = CensoError.MissingUserEmailError())
        }

        val approvalDispositionRequestV2 = ApprovalDispositionRequestV2(
            requestId = requestId,
            approvalDisposition = registerApprovalDisposition.approvalDisposition!!,
            email = userEmail,
            requestType = registerApprovalDisposition.approvalRequestType!!
        )

        val registerApprovalDispositionBody = try {
            approvalDispositionRequestV2.convertToApiBody(encryptionManager, cryptoObject)
        } catch (e: Exception) {
            return Resource.Error(censoError = CensoError.SigningDataError())
        }

        return retrieveApiResource {
            api.approveOrDenyDisposition(
                requestId = approvalDispositionRequestV2.requestId,
                registerApprovalDispositionBody = registerApprovalDispositionBody
            )
        }
    }

    override suspend fun approveOrDenyInitiation(
        requestId: String,
        initialDisposition: InitiationDisposition,
        cryptoObject: CryptoObject,
    ) : Resource<InitiationRequest.InitiateRequestBody> {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (initialDisposition.anyItemNull()) {
            return Resource.Error(censoError = CensoError.DefaultDispositionError())
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            return Resource.Error(censoError = CensoError.MissingUserEmailError())
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
            initiationRequest.convertToApiBody(encryptionManager, cryptoObject)
        } catch (e: Exception) {
            return Resource.Error(censoError = CensoError.SigningDataError())
        }

        return retrieveApiResource {
            api.approveOrDenyInitiation(
                requestId = requestId,
                initiationRequestBody = initiationRequestBody
            )
        }
    }
}
