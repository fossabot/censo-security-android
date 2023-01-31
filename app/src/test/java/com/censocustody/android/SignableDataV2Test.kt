package com.censocustody.android

import com.censocustody.android.data.SignableDataResult
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import org.junit.Assert.assertEquals
import org.junit.Test

class SignableDataV2Test {
    private val deserializer = ApprovalRequestV2Deserializer()

    @Test
    fun testLoginOp() {
        val loginApprovalWalletApproval = deserializer.toObjectWithParsedDetails(exampleRequests[0].trim())
        val disposition = ApprovalDispositionRequestV2(
            "guid",
            ApprovalDisposition.APPROVE,
            loginApprovalWalletApproval.details,
            "email"
        )
        assertEquals(
            disposition.retrieveSignableData(),
            listOf(
                SignableDataResult.Device(
                    "*****".toByteArray(),
                    "*****".toByteArray()
                )
            )
        )
    }

}