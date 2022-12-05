package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.common.UriWrapper

class MockUriWrapper : UriWrapper {
    override fun encode(data: String): String {
        return data
    }

}