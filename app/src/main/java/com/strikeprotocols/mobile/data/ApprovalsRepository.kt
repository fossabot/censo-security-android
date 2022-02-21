package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.strikeLog
import okhttp3.ResponseBody
import javax.inject.Inject

interface ApprovalsRepository {
    suspend fun getApprovals(): ResponseBody?
}

class ApprovalsRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService
) : ApprovalsRepository {

    override suspend fun getApprovals(): ResponseBody? {
        return try {
            api.getApprovals()
        } catch (e: Exception) {
            strikeLog(message = "Expecting error because this is fake; $e")
            null
        }
    }
}
