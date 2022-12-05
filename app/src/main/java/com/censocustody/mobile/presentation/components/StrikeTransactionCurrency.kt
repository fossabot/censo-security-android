package com.censocustody.mobile.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.mobile.R
import com.censocustody.mobile.ui.theme.GreyText
import com.censocustody.mobile.ui.theme.StrikeWhite

@Composable
fun StrikeTransactionCurrency(
    cryptoValue: String,
    currencyEquivalentValue: String,
    contentSpacerHeight: Int,
    fontSize: Int
) {
    Text(
        cryptoValue,
        color = StrikeWhite, fontSize = fontSize.sp, textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(contentSpacerHeight.dp))
    Text(
        "$currencyEquivalentValue ${stringResource(id = R.string.equivalent)}",
        textAlign = TextAlign.Center,
        color = GreyText,
        fontSize = 14.sp
    )
}