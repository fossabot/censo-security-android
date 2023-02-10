package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.common.maskAddress
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun WithdrawalRequestDetailContent(withdrawalRequestUI: WithdrawalRequestUI) {
    ApprovalContentHeader(header = withdrawalRequestUI.header, topSpacing = 24, bottomSpacing = 8)
    ApprovalSubtitle(text = withdrawalRequestUI.subtitle)
    Spacer(modifier = Modifier.height(24.dp))

    val facts = listOfNotNull(
        RowData(
            title = stringResource(R.string.from_wallet),
            value = withdrawalRequestUI.fromAccount,
        ),
        RowData(
            title = stringResource(R.string.destination),
            value = withdrawalRequestUI.toAccount,
        ),
        RowData(
            title = stringResource(R.string.destination_address),
            value = withdrawalRequestUI.address.maskAddress(),
        ),
        if (withdrawalRequestUI.nftMetadataName != null) {
            RowData(
                title = stringResource(R.string.nft_name),
                value = withdrawalRequestUI.nftMetadataName,
            )
        } else null,
    )

    val feeFacts = if (withdrawalRequestUI.replacementFee != null) {
        listOf(
            RowData(
                title = stringResource(R.string.amount),
                value = withdrawalRequestUI.amount.formattedAmountWithSymbol(withdrawalRequestUI.symbol),
            ),
            RowData(
                title = stringResource(R.string.original_fee),
                value = withdrawalRequestUI.fee.formattedAmountWithSymbol(withdrawalRequestUI.symbol),
            ),
            RowData(
                title = stringResource(R.string.new_fee),
                value = withdrawalRequestUI.replacementFee.formattedAmountWithSymbol(withdrawalRequestUI.symbol),
            )
        )
    } else {
        listOf(
            if (withdrawalRequestUI.symbol == "BTC") {
                RowData(
                    title = stringResource(R.string.fee),
                    value = withdrawalRequestUI.fee.formattedAmountWithSymbol(
                        withdrawalRequestUI.feeSymbol ?: withdrawalRequestUI.symbol
                    )
                )
            } else {
                RowData(
                    title = stringResource(R.string.fee_estimate),
                    value = withdrawalRequestUI.fee.formattedUsdEquivalentWithSymbol()
                )
            }
        )
    }

    FactRow(factsData = FactsData(facts = facts + feeFacts))
    Spacer(modifier = Modifier.height(28.dp))
}

data class WithdrawalRequestUI(
    val header: String, val subtitle: String,
    val fromAccount: String, val toAccount: String,
    val amount: ApprovalRequestDetailsV2.Amount,
    val fee: ApprovalRequestDetailsV2.Amount, val address: String,
    val replacementFee: ApprovalRequestDetailsV2.Amount? = null,
    val nftMetadataName: String? = null, val symbol: String, val feeSymbol: String? = null
)