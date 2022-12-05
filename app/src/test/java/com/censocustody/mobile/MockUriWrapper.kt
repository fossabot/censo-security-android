package com.censocustody.mobile

import com.censocustody.mobile.common.UriWrapper

class MockUriWrapper : UriWrapper {
    override fun encode(data: String): String {
        return data
    }

}