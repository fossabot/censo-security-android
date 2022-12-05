package com.strikeprotocols.mobile.presentation.contact_strike

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.ui.theme.StrikePurple
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun ContactStrikeScreen() {
    val localHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxSize().padding(36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.contact_censo_custody),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            color = StrikeWhite
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier
                .clickable {
                    localHandler.openUri("https://help.strikeprotocols.com")
                }
                .padding(24.dp),
            text = stringResource(R.string.get_help),
            color = StrikePurple,
            fontWeight = FontWeight.W500,
            fontSize = 20.sp
        )
    }
}