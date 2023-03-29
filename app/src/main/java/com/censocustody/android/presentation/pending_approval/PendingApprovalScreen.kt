package com.censocustody.android.presentation.pending_approval

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.CensoButton
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.key_management.BackgroundUI
import com.censocustody.android.ui.theme.BackgroundGrey
import com.censocustody.android.ui.theme.ButtonRed
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.ui.theme.TextBlack

@Composable
fun PendingApprovalScreen(
    navController: NavController,
    viewModel: PendingApprovalViewModel = hiltViewModel(),
) {
    val state = viewModel.state

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }

    LaunchedEffect(key1 = state) {
        if (state.sendUserToEntrance is Resource.Success) {
            navController.navigate(Screen.EntranceRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetSendUserToEntrance()
        }

        if (state.verifyUserResult is Resource.Success) {
            viewModel.resetVerifyUserResult()
        }
    }

    BackgroundUI()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 8.dp)
                .shadow(
                    elevation = 5.dp,
                )
                .clip(RoundedCornerShape(4.dp))
                .background(color = BackgroundGrey),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.verifyUserResult !is Resource.Error) {
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    text = stringResource(R.string.waiting_for_device_approval),
                    textAlign = TextAlign.Center,
                    color = TextBlack,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(36.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(44.dp),
                    color = ButtonRed,
                    strokeWidth = 5.dp,
                )
                Spacer(modifier = Modifier.height(36.dp))
            } else {
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.error_retrieving_user),
                    textAlign = TextAlign.Center,
                    color = TextBlack,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(36.dp))
                CensoButton(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    onClick = viewModel::resetVerifyUserResult,
                ) {
                    Text(
                        text = stringResource(id = R.string.try_again),
                        fontSize = 18.sp,
                        color = CensoWhite,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}