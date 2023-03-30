package com.censocustody.android.data

import com.censocustody.android.common.Resource
import com.censocustody.android.common.CensoError
import com.censocustody.android.common.toShareUserId
import com.censocustody.android.data.models.GetShardsResponse
import com.censocustody.android.data.models.RegisterApprovalDisposition
import com.censocustody.android.data.models.Shard
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getApprovalRequests(): Resource<List<ApprovalRequestV2?>>
    suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition,
    ): Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body>
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val encryptionManager: EncryptionManager,
    private val userRepository: UserRepository
) : ApprovalsRepository, BaseRepository() {

    override suspend fun getApprovalRequests(): Resource<List<ApprovalRequestV2?>> =
        retrieveApiResource { api.getApprovalRequests() }

    private suspend fun getShardsFromAPI(
        policyRevisionId: String,
        userId: String
    ): Resource<GetShardsResponse> =
        retrieveApiResource { api.getShards(policyRevisionId = policyRevisionId, userId = userId) }

    override suspend fun approveOrDenyDisposition(
        requestId: String,
        registerApprovalDisposition: RegisterApprovalDisposition,
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

        val shards = retrieveNecessaryShards(
            userEmail = userEmail,
            requestDetails = registerApprovalDisposition.approvalRequestType
        )
        val approvalDispositionRequestV2 = ApprovalDispositionRequestV2(
            requestId = requestId,
            approvalDisposition = registerApprovalDisposition.approvalDisposition!!,
            email = userEmail,
            requestType = registerApprovalDisposition.approvalRequestType!!,
            shards = shards
        )

        val registerApprovalDispositionBody = try {
            approvalDispositionRequestV2.convertToApiBody(encryptionManager)
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

    private suspend fun retrieveNecessaryShards(
        userEmail: String,
        requestDetails: ApprovalRequestDetailsV2?
    ): List<Shard> {
        when (requestDetails) {
            is ApprovalRequestDetailsV2.AddDevice -> {

                if (requestDetails.currentShardingPolicyRevisionGuid == null) return emptyList()

                val shardResponse = getShardsFromAPI(
                    policyRevisionId = requestDetails.currentShardingPolicyRevisionGuid,
                    userId = userEmail.toShareUserId()
                )

                if (shardResponse is Resource.Success) {
                    return shardResponse.data?.shards ?: emptyList()
                } else {
                    throw Exception("Failed to retrieve shards")
                }

            }
            else -> {
                return emptyList()
            }
        }
    }
}

