package com.censocustody.mobile.presentation.semantic_version_check

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.censocustody.mobile.R
import com.censocustody.mobile.common.CensoButton
import com.censocustody.mobile.ui.theme.CensoWhite

@Composable
fun EnforceUpdateScreen() {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.minimum_app_version),
            color = CensoWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        CensoButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            height = 54.dp,
            onClick = {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("http://play.google.com/store/apps/details?id=com.censocustody.mobile")
                    setPackage("com.android.vending")
                }
                try {
                    ContextCompat.startActivity(context, playStoreIntent, null)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.play_store_not_found),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }) {
            Text(
                text = stringResource(id = R.string.update_app),
                color = CensoWhite,
                fontSize = 18.sp
            )
        }
    }
}