package com.censocustody.android.evm

import com.censocustody.android.data.models.evm.EIP712Data
import junit.framework.TestCase.assertEquals
import org.junit.Test

class EIP712DataTest {

    @Test
    fun `test eip712 data`() {
        val data = EIP712Data("{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":1,\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\":{\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}")
        assertEquals("Ether Mail", data.getDomainName())
        assertEquals("0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC", data.getDomainVerifyingContract())
        val entries = data.getMessageEntries()
        assertEquals(3, entries.size)
        assertEquals(
            "from",
            entries[0].name
        )
        assertEquals(
            "Person",
            entries[0].type
        )
        assertEquals(
            "{\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"}",
            entries[0].value.toString()
        )
        assertEquals(
            "to",
            entries[1].name
        )
        assertEquals(
            "Person",
            entries[1].type
        )
        assertEquals(
            "{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"}",
            entries[1].value.toString()
        )
        assertEquals(
            "contents",
            entries[2].name
        )
        assertEquals(
            "string",
            entries[2].type
        )
        assertEquals(
            "\"Hello, Bob!\"",
            entries[2].value.toString()
        )
    }
}