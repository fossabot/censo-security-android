package com.strikeprotocols.mobile.common

import android.content.Context
import com.strikeprotocols.mobile.R

sealed class StrikeError(val errorCode: Int) {

    fun getErrorMessage(context: Context) =
        when (this) {
            is TimeoutError -> {
                context.getString(R.string.request_timed_out)
            }
            is NoInternetError -> {
                context.getString(R.string.not_connected_to_internet)
            }
            is MaintenanceError -> {
                context.getString(R.string.strike_maintenance_message)
            }
            else -> {
                context.getString(R.string.something_went_wrong)
            }
        }

    abstract class ApiError(errorCode: Int, val statusCode: Int) :
        StrikeError(errorCode = errorCode)

    class DefaultApiError(statusCode: Int) : ApiError(errorCode = 10, statusCode = statusCode)
    class NoInternetError(statusCode: Int) : ApiError(errorCode = 20, statusCode = statusCode)
    class TimeoutError(statusCode: Int) : ApiError(errorCode = 30, statusCode = statusCode)
    class MaintenanceError(statusCode: Int) : ApiError(errorCode = 40, statusCode = statusCode)

    abstract class ApprovalDispositionError(errorCode: Int) : StrikeError(errorCode = errorCode)

    class DefaultDispositionError : ApprovalDispositionError(errorCode = 50)
    class SigningDataError : ApprovalDispositionError(errorCode = 60)

    abstract class ApprovalSigningError(errorCode: Int) : StrikeError(errorCode = errorCode)

    class MissingKeyError : ApprovalSigningError(errorCode = 70)
    class MissingUserEmailError : ApprovalSigningError(errorCode = 80)
    class NotEnoughNonceAccountsError : ApprovalSigningError(errorCode = 90)
    class MissingApproverKeyError : ApprovalSigningError(errorCode = 100)
    class MissingSourceKeyError : ApprovalSigningError(errorCode = 110)
}