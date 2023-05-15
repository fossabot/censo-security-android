package com.censocustody.android.data.repository

import com.censocustody.android.common.Resource
import com.censocustody.android.data.api.BrooklynApiService
import com.censocustody.android.data.models.recovery.OrgAdminRecoveryRequestEnvelope
import com.censocustody.android.data.models.recovery.OrgAdminRecoverySignaturesRequest
import javax.inject.Inject

interface OrgRecoveryRepository {
    suspend fun getMyOrgAdminRecoveryRequest(): Resource<OrgAdminRecoveryRequestEnvelope>
    suspend fun registerOrgAdminRecoverySignatures(
        recoveryAddress: String,
        signatures: List<OrgAdminRecoverySignaturesRequest.Signature>,
    ): Resource<Unit>
}

class OrgRecoveryRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
) : OrgRecoveryRepository, BaseRepository() {

    override suspend fun getMyOrgAdminRecoveryRequest(): Resource<OrgAdminRecoveryRequestEnvelope> =
        retrieveApiResource { api.getMyOrgAdminRecoveryRequest() }

    override suspend fun registerOrgAdminRecoverySignatures(
        recoveryAddress: String,
        signatures: List<OrgAdminRecoverySignaturesRequest.Signature>,
    ): Resource<Unit> {
        return retrieveApiResource {
            api.registerOrgAdminRecoverySignatures(
                OrgAdminRecoverySignaturesRequest(
                    recoveryAddress, signatures
                )
            )
        }
    }
}