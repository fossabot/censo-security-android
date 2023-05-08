package com.censocustody.android.data.repository

import com.censocustody.android.common.Resource
import com.censocustody.android.common.exception.CensoError
import com.censocustody.android.data.api.BrooklynApiService
import com.censocustody.android.data.cryptography.EncryptionManager
import com.censocustody.android.data.models.*
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import okhttp3.ResponseBody
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getApprovalRequests(): Resource<List<ApprovalRequestV2?>>
    suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition,
        recoveryShards: Shards,
        reshareShards: Shards
    ): Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body>

    suspend fun retrieveShards(policyRevisionId: String, userId: String? = null) : Resource<GetShardsResponse>

    suspend fun sendWcUri(uri: String): Resource<ResponseBody>
    suspend fun checkIfConnectionHasSessions(topic: String) : Resource<List<WalletConnectTopic>>
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val userRepository: UserRepository
) : ApprovalsRepository, BaseRepository() {

    override suspend fun getApprovalRequests(): Resource<List<ApprovalRequestV2?>> =
        retrieveApiResource { api.getApprovalRequests() }

    override suspend fun retrieveShards(
        policyRevisionId: String,
        userId: String?
    ): Resource<GetShardsResponse> =
        retrieveApiResource { api.getShards(policyRevisionId = policyRevisionId, userId = userId) }

    override suspend fun sendWcUri(uri: String) =
        retrieveApiResource {
            api.walletConnectPairing(WalletConnectPairingRequest(uri = uri))
        }

    override suspend fun checkIfConnectionHasSessions(topic: String) =
        retrieveApiResource {
            api.checkSessionsOnConnectedDApp(topic)
        }

    override suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition,
        recoveryShards: Shards,
        reshareShards: Shards
    ): Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body> {
        // Helper method anyItemNull() will check if any of the disposition properties are null,
        // this allows us to use !! operator later in this method without worrying of NPE
        if (registerApprovalDisposition.anyItemNull()) {
            return Resource.Error(
                exception = Exception("Null data when trying to register disposition"),
                censoError = CensoError.DefaultDispositionError()
            )
        }

        val userEmail = userRepository.retrieveUserEmail()

        if (userEmail.isEmpty()) {
            return Resource.Error(censoError = CensoError.MissingUserEmailError())
        }

        val approvalDispositionRequestV2 = ApprovalDispositionRequestV2(
            requestId = requestId,
            approvalDisposition = registerApprovalDisposition.approvalDisposition!!,
            email = userEmail,
            requestType = registerApprovalDisposition.approvalRequestType!!,
            recoveryShards = recoveryShards.shards,
            reShareShards = reshareShards.shards
        )

        val registerApprovalDispositionBody = try {
            approvalDispositionRequestV2.convertToApiBody(encryptionManager)
        } catch (e: Exception) {
            return Resource.Error(
                exception = e,
                censoError = CensoError.SigningDataError()
            )
        }

        return retrieveApiResource {
            api.approveOrDenyDisposition(
                requestId = approvalDispositionRequestV2.requestId,
                registerApprovalDispositionBody = registerApprovalDispositionBody
            )
        }
    }
}