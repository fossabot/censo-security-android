package com.censocustody.android.common.exception

import android.content.Context
import com.censocustody.android.R

sealed class CensoError(val errorCode: Int) {

    fun getErrorMessage(context: Context) =
        when (this) {
            is TimeoutError -> {
                context.getString(R.string.request_timed_out)
            }
            is NoInternetError -> {
                context.getString(R.string.not_connected_to_internet)
            }
            is MaintenanceError -> {
                context.getString(R.string.censo_maintenance_message)
            }
            else -> {
                null
            }
        }

    abstract class ApiError(errorCode: Int, val statusCode: Int) :
        CensoError(errorCode = errorCode)

    class DefaultApiError(statusCode: Int) : ApiError(errorCode = 10, statusCode = statusCode)
    class NoInternetError(statusCode: Int) : ApiError(errorCode = 20, statusCode = statusCode)
    class TimeoutError(statusCode: Int) : ApiError(errorCode = 30, statusCode = statusCode)
    class MaintenanceError(statusCode: Int) : ApiError(errorCode = 40, statusCode = statusCode)

    abstract class ApprovalDispositionError(errorCode: Int) : CensoError(errorCode = errorCode)

    class DefaultDispositionError : ApprovalDispositionError(errorCode = 50)
    class SigningDataError : ApprovalDispositionError(errorCode = 60)

    abstract class ApprovalSigningError(errorCode: Int) : CensoError(errorCode = errorCode)

    class MissingUserEmailError : ApprovalSigningError(errorCode = 80)

    class FailedRetrieveShards : ApprovalDispositionError(errorCode = 90)
}