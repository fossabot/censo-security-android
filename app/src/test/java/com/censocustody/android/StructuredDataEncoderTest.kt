package com.censocustody.android

import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.web3j.crypto.StructuredDataEncoder

class TestStructuredDataEncoder {
    @Test
    fun `test structured data encoder example which failed to work with web3j v5-0-0`() {
        val data =
            "{\"types\":{\"PermitSingle\":[{\"name\":\"details\",\"type\":\"PermitDetails\"},{\"name\":\"spender\",\"type\":\"address\"},{\"name\":\"sigDeadline\",\"type\":\"uint256\"}],\"PermitDetails\":[{\"name\":\"token\",\"type\":\"address\"},{\"name\":\"amount\",\"type\":\"uint160\"},{\"name\":\"expiration\",\"type\":\"uint48\"},{\"name\":\"nonce\",\"type\":\"uint48\"}],\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}]},\"domain\":{\"name\":\"Permit2\",\"chainId\":\"1\",\"verifyingContract\":\"0x000000000022d473030f116ddee9f6b43ac78ba3\"},\"primaryType\":\"PermitSingle\",\"message\":{\"details\":{\"token\":\"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48\",\"amount\":\"1461501637330902918203684832716283019655932542975\",\"expiration\":\"1687737044\",\"nonce\":\"0\"},\"spender\":\"0xef1c6e67703c7bd7107eed8303fbe6ec2554bf6b\",\"sigDeadline\":\"1685146844\"}}"
        assertNotNull(StructuredDataEncoder(data).hashStructuredData())
    }
}