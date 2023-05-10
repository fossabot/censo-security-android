package com.censocustody.android.data.repository

import com.censocustody.android.common.Resource
import com.censocustody.android.data.api.BrooklynApiService
import com.censocustody.android.data.models.*
import com.censocustody.android.data.models.recovery.OrgAdminRecoveryRequest
import com.censocustody.android.data.models.recovery.OrgAdminRecoverySignaturesRequest
import javax.inject.Inject

interface OrgRecoveryRepository {
    suspend fun getMyOrgAdminRecoveryRequest(): Resource<OrgAdminRecoveryRequest>
    suspend fun registerOrgAdminRecoverySignatures(
        recoveryAddress: String,
        signatures: List<OrgAdminRecoverySignaturesRequest.Signature>,
    ): Resource<Unit>
}

class OrgRecoveryRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
) : OrgRecoveryRepository, BaseRepository() {

    override suspend fun getMyOrgAdminRecoveryRequest(): Resource<OrgAdminRecoveryRequest> =
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