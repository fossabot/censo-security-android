package com.strikeprotocols.mobile.common

import com.strikeprotocols.mobile.data.models.Organization
import com.strikeprotocols.mobile.data.models.VerifyUser

fun generateVerifyUserDummyData() = VerifyUser(
    fullName = "John Doe",
    hasApprovalPermission = false,
    id = "jondoe",
    loginName = "Johnny Doey",
    organization = Organization(
        id = "cryptoco",
        name = "cryptology"
    ),
    publicKeys = emptyList(),
    useStaticKey = false
)

fun generateKeyPairDummyData(): Pair<String, String> = "Key: public key" to "Value: private key"