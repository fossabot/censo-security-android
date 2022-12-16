package com.censocustody.android.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.BuildConfig
import com.censocustody.android.R
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.AccountRow
import com.censocustody.android.presentation.components.CenteredTopAppBar
import com.censocustody.android.ui.theme.*
import java.util.*


@Composable
fun AccountScreen(
    navController: NavController,
    accountViewModel: AccountViewModel = hiltViewModel()
) {

    val appVersionText =
        "${stringResource(R.string.app_version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})${getAppVersionSuffix()}"

    val localHandler = LocalUriHandler.current

    val accountState = accountViewModel.state

    DisposableEffect(key1 = accountViewModel) {
        accountViewModel.onStart()
        onDispose { }
    }

    LaunchedEffect(key1 = accountState) {
        if (accountState.logoutResult is Resource.Success) {
            navController.navigate(Screen.EntranceRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            accountViewModel.resetLogoutResource()
        }
    }

    //region Screen Content
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenteredTopAppBar(
                title = stringResource(id = R.string.user),
                onAppBarIconClick = { navController.navigateUp() },
                showNavIcon = false,
                navigationIcon = Icons.Filled.ArrowBack,
                navigationIconContentDes = "",
                actions = {
                    Box(modifier = Modifier
                        .clickable { navController.navigateUp() }
                        .padding(horizontal = 12.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .fillMaxHeight()) {
                        Text(
                            modifier = Modifier.align(alignment = Alignment.Center),
                            text = stringResource(id = R.string.done),
                            color = CensoWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BackgroundBlack)
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    ) {
                    Spacer(modifier = Modifier.height(44.dp))
                    AccountRow(
                        titleColor = CensoWhite,
                        title = stringResource(R.string.email),
                        value = accountState.email
                    )
                    Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
                    AccountRow(
                        titleColor = CensoWhite,
                        title = stringResource(R.string.name),
                        value = accountState.name
                    )
                    Spacer(modifier = Modifier.height(64.dp))
                    Text(
                        color = CensoWhite,
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.security_notice),
                        letterSpacing = 0.25.sp,
                        fontWeight = FontWeight.W500,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = CensoWhite,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.25.sp,
                        fontSize = 14.sp,
                        text = stringResource(R.string.security_message)
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        modifier = Modifier
                            .clickable {
                                localHandler.openUri("https://help.censocustody.com")
                            }
                            .padding(24.dp),
                        text = stringResource(R.string.get_help),
                        color = CensoTextBlue,
                        fontWeight = FontWeight.W500,
                        fontSize = 20.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                    Text(
                        text = appVersionText,
                        color = CensoWhite,
                        letterSpacing = 0.25.sp,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        onClick = accountViewModel::logout,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccountButtonRed),
                        enabled = true
                    ) {
                        Text(
                            modifier = Modifier.padding(all = 4.dp),
                            text = stringResource(id = R.string.sign_out),
                            color = SignOutRed,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if(accountState.logoutResult is Resource.Loading) {
                Box(modifier = Modifier.fillMaxSize().background(color = Color.Black.copy(alpha = 0.25f)).clickable {  }) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(alignment = Alignment.Center).size(60.dp),
                        color = CensoWhite
                    )
                }
            }
        }
    )
}

fun getAppVersionSuffix(): String {
    return if (BuildConfig.BUILD_TYPE == "release") {
        ""
    } else {
        " ${BuildConfig.BUILD_TYPE.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
    }
}