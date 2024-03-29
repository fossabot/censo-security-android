package com.censocustody.android.common.exception

import java.lang.Exception

class NoInternetException : Exception(NO_INTERNET_ERROR) {
    companion object {
        const val NO_INTERNET_ERROR = "Not Connected to Internet"
    }
}