package com.censocustody.android.data.models

data class BootstrapDevice(
    val publicKey: String,
    val signature: String
)

// POST to v1/bootstrap-user-devices with this payload to register
// user device key (and image), bootstrap key and signer keys for the bootstrap user for an org
data class BootstrapUserDeviceAndSigners(
    val userDevice: UserDevice,
    val bootstrapDevice: BootstrapDevice,
    val signersInfo: Signers,
)