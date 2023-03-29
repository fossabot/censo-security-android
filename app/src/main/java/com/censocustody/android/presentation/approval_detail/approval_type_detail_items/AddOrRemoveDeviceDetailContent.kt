package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.data.models.DeviceType
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun AddOrRemoveDeviceDetailContent(
    header: String,
    userDevice: UserDevice
) {
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)

    Spacer(modifier = Modifier.height(24.dp))
    FactRow(
        factsData = FactsData(
            title = LocalContext.current.getString(R.string.user_device),
            facts = listOf(
                RowData(
                    title = userDevice.name,
                    value = userDevice.email,
                    userImage = userDevice.jpegThumbnail,
                    userRow = true
                ),
                RowData(
                    title = LocalContext.current.getString(R.string.device_type),
                    value = userDevice.deviceType.description(),
                ),
                RowData(
                    title = LocalContext.current.getString(R.string.device_identifier),
                    value = userDevice.deviceGuid,
                )
            )
        )
    )
    Spacer(modifier = Modifier.height(28.dp))
}

data class UserDevice(
    val name: String,
    val email: String,
    val jpegThumbnail: String,
    val deviceGuid: String,
    val deviceKey: String,
    val deviceType: DeviceType,
)

fun ApprovalRequestDetailsV2.AddDevice.toUserDevice(): UserDevice {
    return(UserDevice(this.name, this.email, this.jpegThumbnail, this.deviceGuid, this.deviceKey, this.deviceType))
}

fun ApprovalRequestDetailsV2.RemoveDevice.toUserDevice(): UserDevice {
    return(UserDevice(this.name, this.email, this.jpegThumbnail, this.deviceGuid, this.deviceKey, this.deviceType))
}
