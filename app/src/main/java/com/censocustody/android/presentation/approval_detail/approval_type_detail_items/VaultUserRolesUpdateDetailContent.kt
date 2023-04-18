package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.common.toVaultName
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData
import com.censocustody.android.R

@Composable
fun VaultUserRolesUpdateDetailContent(
    update: ApprovalRequestDetailsV2.VaultUserRolesUpdate
) {
    val header = update.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)
    ApprovalSubtitle(text = update.vaultName.toVaultName(LocalContext.current), fontSize = 20.sp)

    FactRow(
        factsData = FactsData(
            title = LocalContext.current.getString(R.string.vault_user_roles),
            facts = update.userRoles.map { userRole ->
                RowData.UserRole(
                    name = userRole.name,
                    email = userRole.email,
                    image = userRole.jpegThumbnail,
                    role = LocalContext.current.getString(when (userRole.role) {
                        ApprovalRequestDetailsV2.VaultUserRoleEnum.Viewer -> R.string.vault_user_role_viewer
                        ApprovalRequestDetailsV2.VaultUserRoleEnum.TransactionSubmitter -> R.string.vault_user_role_transaction_submitter
                    })
                )
            }
        )
    )

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
@Preview
fun VaultUserRolesUpdateDetailContentPreview() {
    VaultUserRolesUpdateDetailContent(
        ApprovalRequestDetailsV2.VaultUserRolesUpdate(
            "Main",
            listOf(
                ApprovalRequestDetailsV2.VaultUserRole("User 1", "user1@org.com", null, ApprovalRequestDetailsV2.VaultUserRoleEnum.TransactionSubmitter),
                ApprovalRequestDetailsV2.VaultUserRole("User 2", "user2@org.com", null, ApprovalRequestDetailsV2.VaultUserRoleEnum.Viewer)
            )
        )
    )
}