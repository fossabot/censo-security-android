package com.strikeprotocols.mobile.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.StrikeUserData
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.AccountRow
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsViewModel
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar
import com.strikeprotocols.mobile.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun AccountScreen(
    navController: NavController,
    approvalsViewModel: ApprovalsViewModel
) {

    val appVersionText =
        "${stringResource(R.string.app_version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})${getAppVersionSuffix()}"

    val localHandler = LocalUriHandler.current

    val approvalsState = approvalsViewModel.state

    LaunchedEffect(key1 = approvalsState) {
        if (approvalsState.logoutResult is Resource.Success) {
            navController.navigate(Screen.SignInRoute.route) {
                popUpTo(Screen.ApprovalListRoute.route) {
                    inclusive = true
                }
            }
            approvalsViewModel.resetLogoutResource()
        }
    }

    //region Screen Content
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            StrikeTopAppBar(
                title = "",
                onAppBarIconClick = { navController.navigateUp() },
                navigationIconContentDes = stringResource(id = R.string.content_des_back_icon),
                navigationIcon = Icons.Rounded.ArrowBack,
                actions = {
                    TextButton(onClick = { navController.navigateUp() }) {
                        Text(stringResource(R.string.done), color = StrikeWhite, fontSize = 18.sp)
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
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    ) {
                    Spacer(modifier = Modifier.height(44.dp))
                    //todo: str-257 use value from cached StrikeUser data. That is in bip-39-auth branch.
                    AccountRow(
                        titleColor = StrikeWhite,
                        title = stringResource(R.string.email),
                        value = approvalsState.email
                    )
                    Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
                    //todo: str-257 use value from cached StrikeUser data. That is in bip-39-auth branch.
                    AccountRow(
                        titleColor = StrikeWhite,
                        title = stringResource(R.string.name),
                        value = approvalsState.name
                    )
                    Spacer(modifier = Modifier.height(64.dp))
                    Text(
                        color = StrikeWhite,
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.security_notice),
                        letterSpacing = 0.25.sp,
                        fontWeight = FontWeight.W500,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = StrikeWhite,
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
                                localHandler.openUri("https://help.strikeprotocols.com")
                            }
                            .padding(24.dp),
                        text = stringResource(R.string.get_help),
                        color = StrikePurple,
                        fontWeight = FontWeight.W500,
                        fontSize = 20.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                    Text(
                        text = appVersionText,
                        color = StrikeWhite,
                        letterSpacing = 0.25.sp,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        onClick = approvalsViewModel::logout,
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