package com.censocustody.android.presentation.contact_censo

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
import com.censocustody.android.ui.theme.CensoTextBlue
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.R
import com.censocustody.android.ui.theme.TextBlack

@Composable
fun ContactCensoScreen() {
    val localHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxSize().padding(36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.contact_censo),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            color = TextBlack
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier
                .clickable {
                    localHandler.openUri("https://help.censo.co")
                }
                .padding(24.dp),
            text = stringResource(R.string.get_help),
            color = CensoTextBlue,
            fontWeight = FontWeight.W500,
            fontSize = 20.sp
        )
    }
}
