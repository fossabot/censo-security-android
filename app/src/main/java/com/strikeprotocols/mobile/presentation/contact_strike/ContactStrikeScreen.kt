package com.strikeprotocols.mobile.presentation.contact_strike

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun ContactStrikeScreen() {
    Text(
        modifier = Modifier.fillMaxSize(),
        text = stringResource(R.string.contact_strike),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h2,
        color = StrikeWhite
    )
}