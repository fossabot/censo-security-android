package com.censocustody.mobile.common

import android.net.Uri

interface UriWrapper {
    fun encode(data: String): String
}

class AndroidUriWrapper : UriWrapper {
    override fun encode(data: String): String = Uri.encode(data)
}