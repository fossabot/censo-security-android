package com.censocustody.android.common

import com.censocustody.android.common.exception.CensoError

sealed class Resource<out T>(
    val data: T? = null,
    val exception: Exception? = null,
    val censoError: CensoError? = null,
) {
    object Uninitialized : Resource<Nothing>()
    class Success<out T>(data: T?) : Resource<T>(data)
    class Error<out T>(
        data: T? = null,
        exception: Exception? = null,
        censoError: CensoError? = null
    ) : Resource<T>(data, exception, censoError = censoError)

    class Loading<out T>(data: T? = null) : Resource<T>(data)
}