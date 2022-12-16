package com.censocustody.android

import com.google.gson.JsonParser
import com.censocustody.android.data.MultipleAccountsDeserializer
import com.censocustody.android.data.models.Nonce
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ParseNonceTest {


    private val deserializer = MultipleAccountsDeserializer()

    val response = """
        {"jsonrpc":"2.0","result":{"context":{"slot":402},"value":[{"data":["AAAAAAEAAADVJZp1iY5cFvGwZ1xJap+O503XaH8jS6k8D/Cd/uivNCdJE0k9Ajpn/cEy3TSbbYl5dgEsceAKtAIKa1nvxf6QiBMAAAAAAAA=","base64"],"executable":false,"lamports":1447680,"owner":"11111111111111111111111111111111","rentEpoch":0},{"data":["AAAAAAEAAADVJZp1iY5cFvGwZ1xJap+O503XaH8jS6k8D/Cd/uivNPowQba3TsNG8RNMBrZK/RZighANP8iUyL4yx85rzJDLiBMAAAAAAAA=","base64"],"executable":false,"lamports":1447680,"owner":"11111111111111111111111111111111","rentEpoch":0},{"data":["AAAAAAEAAADVJZp1iY5cFvGwZ1xJap+O503XaH8jS6k8D/Cd/uivNIQJ8qqFaevM1IJGpCc4YNY67rnrwwyslHS6t+htayrBiBMAAAAAAAA=","base64"],"executable":false,"lamports":1447680,"owner":"11111111111111111111111111111111","rentEpoch":0},]},"id":"5c0c319a-4482-45c3-b9b9-d54975c1c4eb"}
    """.trimIndent()

    val expectedresponseData = listOf(
        Nonce("3eMXeaEkwY5C6UxB6jsMjRGAsDcozgMCdjbZoowSTXZy"),
        Nonce("HqdbyB576ggyavQPaodKiS8XNPHBoo95rb75Le7XzXrr"),
        Nonce("9tRccoR8XEVfP32LEZYtZuR8YFYFbPg9kCKnBicdPQHN")
    )

    @Before
    fun setUp() {
    }

    @Test
    fun testExtractNonces() {
        val responseAsJsonElement = JsonParser.parseString(response.trim())
        val parsedResponse = deserializer.parseData(responseAsJsonElement)

        assertEquals(parsedResponse.nonces.size, expectedresponseData.size)
        assertEquals(parsedResponse.nonces, expectedresponseData)
    }

}