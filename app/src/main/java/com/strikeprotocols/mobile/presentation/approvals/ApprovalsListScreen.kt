package com.strikeprotocols.mobile.presentation.approvals

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun ApprovalsListScreen(
    viewModel: ApprovalsViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.holder_text),
            fontSize = 72.sp,
            color = StrikeWhite
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(
            onClick = {
                viewModel.checkToken()
            }) {
            Text("Go Check Access Token")
        }
    }
}