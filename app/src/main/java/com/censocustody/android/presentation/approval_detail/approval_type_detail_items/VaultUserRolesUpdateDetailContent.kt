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
                ApprovalRequestDetailsV2.VaultUserRole("User 2", "user2@org.com", "/9j/4AAQSkZJRgABAQAASABIAAD/4QCARXhpZgAATU0AKgAAAAgABAESAAMAAAABAAEAAAEaAAUAAAABAAAAPgEbAAUAAAABAAAARodpAAQAAAABAAAATgAAAAAAAABIAAAAAQAAAEgAAAABAAOgAQADAAAAAQABAACgAgAEAAAAAQAAAQKgAwAEAAAAAQAAAQIAAAAA/+EJIWh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8APD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iWE1QIENvcmUgNi4wLjAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIi8+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPD94cGFja2V0IGVuZD0idyI/PgD/wAARCAECAQIDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW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", ApprovalRequestDetailsV2.VaultUserRoleEnum.Viewer)
            )
        )
    )
}