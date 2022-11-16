package com.strikeprotocols.mobile.presentation.device_registration

import com.strikeprotocols.mobile.common.Resource

data class DeviceRegistrationState(
    val addUserDevice: Resource<Boolean> = Resource.Uninitialized
)