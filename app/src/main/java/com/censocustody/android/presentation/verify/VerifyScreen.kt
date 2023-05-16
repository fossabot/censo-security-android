package com.censocustody.android.presentation.verify

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.censocustody.android.ui.theme.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.censocustody.android.common.Resource
import com.censocustody.android.common.wrapper.BaseWrapper

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")

@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        onDispose { }
    }

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
    }
    //endregion

    Scaffold(
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Data to sign: 3yQ")
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Signed Data:\n${BaseWrapper.encodeToBase64(state.signedData)}",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Signature verified:\n${if (state.verified == Resource.Uninitialized) "" else state.verified.data}",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = viewModel::signData) {
                    Text("Sign Data", color = CensoWhite)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = viewModel::verifyData) {
                    Text("Verify Signature", color = CensoWhite)
                }
            }
        }
    )
}