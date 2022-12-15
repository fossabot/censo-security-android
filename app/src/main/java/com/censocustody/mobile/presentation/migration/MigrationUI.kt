package com.censocustody.mobile.presentation.migration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.censocustody.mobile.R
import com.censocustody.mobile.presentation.key_management.GradientBackgroundUI
import com.censocustody.mobile.presentation.key_management.SmallAuthFlowButton
import com.censocustody.mobile.ui.theme.CensoWhite
import com.censocustody.mobile.ui.theme.UnfocusedGrey

@Composable
fun MigrationUI(
    errorEnabled: Boolean,
    errorMessage: String? = null,
    retry: () -> Unit,
) {

    GradientBackgroundUI()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {

        if (errorEnabled) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage ?: stringResource(R.string.something_went_wrong),
                    color = CensoWhite,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.23.sp,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                SmallAuthFlowButton(
                    modifier = Modifier.wrapContentWidth(),
                    text = stringResource(R.string.retry),
                ) {
                    retry()
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(width = 1.5.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                        .background(color = Color.Black)
                        .zIndex(2.5f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.migration_loading_text),
                        textAlign = TextAlign.Center,
                        color = CensoWhite,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CensoWhite,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}