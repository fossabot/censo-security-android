package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseRepository {

    /**
     * Wrap all API calls to return as a Resource.
     *
     * Handle specific HTTP codes and exceptions.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> retrieveApiResource(apiToBeCalled: suspend () -> Response<T>): Resource<T> {

        return withContext(Dispatchers.IO) {
            try {
                val response: Response<T> = apiToBeCalled()

                if (response.isSuccessful) {
                    Resource.Success(data = response.body())
                } else {
                    if (response.code() == MAINTENANCE_CODE) {
                        Resource.Error(strikeError = StrikeError.MaintenanceError(statusCode = MAINTENANCE_CODE))
                    } else {
                        Resource.Error(
                            strikeError = StrikeError.DefaultApiError(statusCode = response.code())
                        )
                    }
                }
            } catch (e: SocketTimeoutException) {
                Resource.Error(
                    exception = e,
                    strikeError = StrikeError.TimeoutError(statusCode = TIMEOUT_CODE)
                )
            } catch (e: IOException) {
                Resource.Error(
                    exception = e,
                    strikeError = StrikeError.NoInternetError(statusCode = NO_CODE)
                )
            } catch (e: Exception) {
                Resource.Error(
                    exception = e,
                    strikeError = StrikeError.DefaultApiError(statusCode = NO_CODE)
                )
            }
        }
    }

    companion object {
        const val MAINTENANCE_CODE = 418
        const val TIMEOUT_CODE = 408
        const val BAD_REQUEST_CODE = 400
        const val NO_CODE = -1
        const val TOO_MANY_REQUESTS_CODE = 429
        const val UNAUTHORIZED = 401
    }

}