package com.censocustody.android.presentation.maintenance

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
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
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.approvals.NavIconTopBar
import com.censocustody.android.presentation.key_management.BackgroundUI
import com.censocustody.android.ui.theme.BackgroundGrey
import com.censocustody.android.ui.theme.ButtonRed
import com.censocustody.android.ui.theme.TextBlack

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MaintenanceScreen(
    navController: NavController,
    viewModel: MaintenanceViewModel = hiltViewModel(),
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

    Scaffold(
        topBar = {
            NavIconTopBar(
                title = stringResource(R.string.maintenance),
                onAppBarIconClick = { navController.navigate(Screen.AccountRoute.route) },
                navigationIcon = Icons.Rounded.AccountCircle,
                navigationIconContentDes = stringResource(id = R.string.content_des_account_icon),
                showNavIcon = state.userLoggedIn
            )
        },
        content = { _ ->
            BackgroundUI()
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        text = stringResource(R.string.checking_maintenance_mode),
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
                }
            }
        }
    )
}