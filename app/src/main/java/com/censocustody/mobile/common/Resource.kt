package com.censocustody.mobile.common

sealed class Resource<out T>(
    val data: T? = null,
    val exception: Exception? = null,
    val strikeError: StrikeError? = null,
) {
    object Uninitialized : Resource<Nothing>()
    class Success<out T>(data: T?) : Resource<T>(data)
    class Error<out T>(
        data: T? = null,
        exception: Exception? = null,
        strikeError: StrikeError? = null
    ) : Resource<T>(data, exception, strikeError = strikeError)

    class Loading<out T>(data: T? = null) : Resource<T>(data)
}