package com.censocustody.android

import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.Organization
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletPublicKey
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer

fun getLoginApprovalV2(): ApprovalRequestV2 {
    val deserializer = ApprovalRequestV2Deserializer()
    return deserializer.toObjectWithParsedDetails(exampleRequests[0].trim())
}

fun getEthereumWithdrawalRequestApprovalV2() : ApprovalRequestV2 {
    return getFullListOfApprovalItems()
        .first { it.details is ApprovalRequestDetailsV2.EthereumWithdrawalRequest }
}

fun getVerifyUser() = VerifyUser(
    fullName = "John Doe",
    hasApprovalPermission = false,
    id = "jondoe",
    loginName = "Johnny Doey",
    organization = Organization(
        id = "crypto",
        name = "cryptology"
    ),
    publicKeys = listOf(WalletPublicKey(key = ExampleMnemonicAndKeys.PUBLIC_KEY, chain = Chain.censo)),
    useStaticKey = false,
    deviceKey = ""
)

class ExampleMnemonicAndKeys {
    companion object {
        val MNEMONIC = "lizard size puppy joke venue census need net produce age all proof opinion promote setup flight tortoise multiply blanket problem defy arrest switch also"
        val BAD_MNEMONIC = "lizard trigger puppy joke venue census need net produce age all proof opinion promote setup flight tortoise multiply blanket problem defy arrest switch also"
        val PRIVATE_KEY = "xprvA2mmuoFKSeeWa7CaZwqLBL6Xuvz9q5XwGh3imXraW2XUeDhy4GA7SJkbgtWZV2vz3ctDzR4dMAs4um1Yj2Ad7iEaonyUzd5JicxR1gprJuY"
        val PUBLIC_KEY = "xpub6Fm8KJnDH2ConbH3fyNLYU3GTxpeEYFnduyKZvGC4N4TX237boUMz755YB3T18k1KdYrboxCRtiBppA3DmWXqmxebjyzoyyWrzXRTDeVoax"
    }
}

fun getNonce() = "7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn"

enum class ResourceState {
    SUCCESS, ERROR
}