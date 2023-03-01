package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.common.convertSecondsIntoReadableText
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.retrieveRowData
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.R
import com.censocustody.android.common.toVaultName
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.approval_type_row_items.formattedUsdEquivalentWithSymbol
import com.censocustody.android.presentation.components.RowData

@Composable
fun VaultConfigPolicyUpdateDetailContent(
    vaultPolicyUpdate: ApprovalRequestDetailsV2.VaultPolicyUpdate,
) {
    val header = vaultPolicyUpdate.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)
    ApprovalSubtitle(text = vaultPolicyUpdate.vaultName.toVaultName(LocalContext.current), fontSize = 20.sp)


    val approverRowInfoData = generatePolicyRows(
        policy = vaultPolicyUpdate.approvalPolicy,
        chainFees = vaultPolicyUpdate.chainFees,
        context = LocalContext.current,
        isOrgPolicy = false
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (approvalRow in approverRowInfoData) {
            FactRow(
                factsData = approvalRow
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

fun generatePolicyRows(
    policy: ApprovalRequestDetailsV2.VaultApprovalPolicy,
    chainFees: List<ApprovalRequestDetailsV2.ChainFee>,
    context: Context,
    isOrgPolicy: Boolean
): List<FactsData> {
    val approvalsRequired = policy.approvalsRequired.toInt().toString()

    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvals Row
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals),
        facts = listOf(
            RowData(
                title = context.getString(R.string.approvals_required),
                value = approvalsRequired,
            ),
            RowData(
                title = context.getString(R.string.approval_expiration),
                value = convertSecondsIntoReadableText(policy.approvalTimeout.toInt(), context),
            )
        )
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion

    //region Approvers Row
    val approversList = policy.approvers.retrieveRowData()
    if (approversList.isEmpty()) {
        approversList.add(
            RowData(
                title = context.getString(R.string.no_approvers_text),
                value = ""
            )
        )
    }

    val approverRow = FactsData(
        title = context.getString(if (isOrgPolicy) R.string.org_admins else R.string.vault_managers),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    if (chainFees.isNotEmpty()) {
        approverRowInfoData.add(
            FactsData(
                title = context.getString(R.string.fees),
                facts = chainFees.map {
                    RowData(
                        title = "${it.chain.label()} ${context.getString(R.string.fee_estimate)}",
                        value = it.fee.formattedUsdEquivalentWithSymbol()
                    )
                }
            )
        )
    }

    return approverRowInfoData
}