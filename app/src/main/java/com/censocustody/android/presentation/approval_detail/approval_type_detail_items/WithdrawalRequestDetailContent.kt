package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.common.maskAddress
import com.censocustody.android.data.models.approval.*
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData

@Composable
fun WithdrawalRequestDetailContent(
    withdrawalRequest: ApprovalRequestDetails.WithdrawalRequest
) {
    val symbolAndAmountInfo = withdrawalRequest.symbolAndAmountInfo

    val header = withdrawalRequest.getHeader(LocalContext.current)
    val subtitle = if (withdrawalRequest.symbolAndAmountInfo.replacementFee == null) {
        withdrawalRequest.symbolAndAmountInfo.getUSDEquivalentText(
            context = LocalContext.current,
            hideSymbol = true
        )
    } else {
        stringResource(R.string.bump_fee_request_approval_details_subtitle)
    }
    val fromAccount = withdrawalRequest.account.name
    val toAccount = withdrawalRequest.destination.name
    val address = withdrawalRequest.destination.address

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    ApprovalSubtitle(text = subtitle)
    Spacer(modifier = Modifier.height(24.dp))

    val facts = listOfNotNull(
        Pair(
            stringResource(R.string.from_wallet),
            fromAccount
        ),
        Pair(
            stringResource(R.string.destination),
            toAccount
        ),
        Pair(
            stringResource(R.string.destination_address),
            address.maskAddress()
        ),
        if (symbolAndAmountInfo.symbolInfo.nftMetadata != null) {
            Pair(
                stringResource(R.string.nft_name),
                symbolAndAmountInfo.symbolInfo.nftMetadata.name
            )
        } else null,
    )

    val feeFacts = if (symbolAndAmountInfo.fee != null && symbolAndAmountInfo.replacementFee != null) {
        listOf(
            Pair(
                stringResource(R.string.amount),
                symbolAndAmountInfo.formattedAmountWithSymbol()
            ),
            Pair(
                stringResource(R.string.original_fee),
                symbolAndAmountInfo.fee.formattedAmountWithSymbol()
            ),
            Pair(
                stringResource(R.string.new_fee),
                symbolAndAmountInfo.replacementFee.formattedAmountWithSymbol()
            )
        )
    } else if (symbolAndAmountInfo.fee != null) {
        listOf(
            Pair(
                stringResource(R.string.fee),
                symbolAndAmountInfo.fee.formattedAmountWithSymbol()
            )
        )
    } else {
        emptyList()
    }

    FactRow(factsData = FactsData(facts = facts + feeFacts))
    Spacer(modifier = Modifier.height(28.dp))
}

class WithdrawalRequestDetailParameterProvider : PreviewParameterProvider<ApprovalRequestDetails.WithdrawalRequest> {
    override val values: Sequence<ApprovalRequestDetails.WithdrawalRequest>
        get() = sequenceOf(
            ApprovalRequestDetails.WithdrawalRequest(
                type = ApprovalType.WITHDRAWAL_TYPE.value,
                account = AccountInfo(
                    name = "Wallet 1",
                    identifier = "5fb4556a-6de5-4a80-ac0e-6def9826384f",
                    accountType = AccountType.BalanceAccount,
                    address = "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2"
                ),
                symbolAndAmountInfo = SymbolAndAmountInfo(
                    symbolInfo = SymbolInfo(symbol = "BTC", symbolDescription = "Bitcoin", tokenMintAddress = null),
                    amount = "1.000000",
                    nativeAmount = "1.000000",
                    usdEquivalent = "20000.00",
                    fee = Fee(
                        symbolInfo = SymbolInfo(symbol = "BTC", symbolDescription = "Bitcoin", tokenMintAddress = null),
                        amount = "0.001",
                        usdEquivalent = "20.00"
                    )
                ),
                destination = DestinationAddress(
                    name = "Wallet 2",
                    subName = null,
                    address = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                    tag = null
                ),
                signingData = SigningData.BitcoinSigningData(0, BitcoinTransaction(0, emptyList(), emptyList(), 123L))
            ),
            ApprovalRequestDetails.WithdrawalRequest(
                type = ApprovalType.WITHDRAWAL_TYPE.value,
                account = AccountInfo(
                    name = "Wallet 1",
                    identifier = "5fb4556a-6de5-4a80-ac0e-6def9826384f",
                    accountType = AccountType.BalanceAccount,
                    address = "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2"
                ),
                symbolAndAmountInfo = SymbolAndAmountInfo(
                    symbolInfo = SymbolInfo(symbol = "BTC", symbolDescription = "Bitcoin", tokenMintAddress = null),
                    amount = "1.000000",
                    nativeAmount = "1.000000",
                    usdEquivalent = "20000.00",
                    fee = Fee(
                        symbolInfo = SymbolInfo(symbol = "BTC", symbolDescription = "Bitcoin", tokenMintAddress = null),
                        amount = "0.001",
                        usdEquivalent = "20.00"
                    ),
                    replacementFee = Fee(
                        symbolInfo = SymbolInfo(symbol = "BTC", symbolDescription = "Bitcoin", tokenMintAddress = null),
                        amount = "0.002",
                        usdEquivalent = "40.00"
                    )
                ),
                destination = DestinationAddress(
                    name = "Wallet 2",
                    subName = null,
                    address = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                    tag = null
                ),
                signingData = SigningData.BitcoinSigningData(0, BitcoinTransaction(0, emptyList(), emptyList(), 123L))
            )
        )
}

@Preview
@Composable
fun WithdrawalRequestDetailContentPreview(@PreviewParameter(WithdrawalRequestDetailParameterProvider::class) request: ApprovalRequestDetails.WithdrawalRequest) {
    WithdrawalRequestDetailContent(request)
}