package com.censocustody.android

import com.censocustody.android.common.UriWrapper

class MockUriWrapper : UriWrapper {
    override fun encode(data: String): String {
        return data
    }

}