package com.strikeprotocols.mobile.presentation.approval_detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.BiometricUtil.createBioPrompt
import com.strikeprotocols.mobile.common.BiometricUtil.getBasicBiometricPromptBuilder
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.convertSecondsIntoCountdownText
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_components.ApprovalDetailsTransferContent
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar
import com.strikeprotocols.mobile.ui.theme.*
import java.util.*

@Composable
fun ApprovalDetailsScreen(
    navController: NavController,
    viewModel: ApprovalDetailsViewModel = hiltViewModel(),
    approval: WalletApproval?
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    fun showToast(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    val promptInfo = getBasicBiometricPromptBuilder(context).build()

    val bioPrompt = createBioPrompt(
        fragmentActivity = context,
        onSuccess = {
            showToast("Authentication success")
            //Let VM handle this
            viewModel.registerApprovalDisposition()
        },
        onFail = {
            showToast("Authentication failed")
            //Let VM handle this
        }
    )

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(approval)
        onDispose { viewModel.onStop() }
    }

    LaunchedEffect(key1 = state) {
        if (state.triggerBioPrompt) {
            viewModel.resetPromptTrigger()
            bioPrompt.authenticate(promptInfo)
        }

        if (state.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success) {
            viewModel.resetApprovalDispositionAPICalls()
            showToast("registered approval disposition")
        }
        if (state.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error) {
            showToast("Failed to registered approval disposition")
            viewModel.resetApprovalDispositionAPICalls()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ApprovalDetailsTopAppBar(
                title = stringResource(id = R.string.transfer_details),
                onAppBarIconClick = { navController.navigateUp() },
                navigationIcon = Icons.Rounded.ArrowBack,
                navigationIconContentDes = stringResource(id = R.string.content_des_back_icon)
            )
        },
        content = {
            ApprovalDetails(
                onApproveClicked = {
                    viewModel.setShouldDisplayConfirmDispositionDialog(
                        isApproving = true,
                        dialogTitle = "Confirm Approval",
                        dialogText = "Please confirm you want to approve this transfer"
                    )
                },
                onDenyClicked = {
                    viewModel.setShouldDisplayConfirmDispositionDialog(
                        isApproving = false,
                        dialogTitle = "Confirm Deny",
                        dialogText = "Please confirm you want to deny this transfer"
                    )
                },
                timeRemainingInSeconds = state.approval?.approvalTimeoutInSeconds ?: 0,
                isLoading = state.loadingData
            )

            if (state.shouldDisplayConfirmDispositionDialog != null) {
                state.shouldDisplayConfirmDispositionDialog.let { safeDialogDetails ->
                    //TODO Refine this after the flow is done
                    ConfirmDispositionAlertDialog(
                        dialogTitle = safeDialogDetails.dialogTitle,
                        dialogText = safeDialogDetails.dialogText,
                        onConfirm = {
                            strikeLog(message = "Confirming disposition")
                            viewModel.resetShouldDisplayConfirmDispositionDialog()
                            viewModel.setPromptTrigger()
                        },
                        onDismiss = {
                            strikeLog(message = "Dismissing dialog")
                            viewModel.resetShouldDisplayConfirmDispositionDialog()
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun ApprovalDetailsTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String
) {
    StrikeTopAppBar(
        title = title,
        onAppBarIconClick = { onAppBarIconClick() },
        navigationIcon = navigationIcon,
        navigationIconContentDes = navigationIconContentDes
    )
}

@Composable
fun ApprovalDetails(
    onApproveClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    timeRemainingInSeconds: Int,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ApprovalDetailsTimer(timeRemainingInSeconds = timeRemainingInSeconds)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ApprovalDetailsTransferContent()

            ApprovalDetailsButtons(
                onApproveClicked = { onApproveClicked() },
                onDenyClicked = { onDenyClicked() },
                isLoading = isLoading
            )
        }
    }

    if(isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = StrikeWhite
            )
        }
    }
}

@Composable
fun ApprovalDetailsTimer(timeRemainingInSeconds: Int) {
    val timerFinished = timeRemainingInSeconds <= 0

    val background = if (timerFinished) DenyRed else SectionBlack
    val text = if (timerFinished) stringResource(R.string.approval_expired) else
        "${stringResource(id = R.string.expires_in)} ${convertSecondsIntoCountdownText(timeRemainingInSeconds)}"

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(vertical = 6.dp),
        text = text,
        textAlign = TextAlign.Center,
        color = StrikeWhite,
        fontSize = 18.sp
    )
}

@Composable
fun ApprovalDetailsButtons(
    onApproveClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    isLoading: Boolean
) {
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onClick = { onDenyClicked() },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(backgroundColor = DenyRedBackground),
        enabled = !isLoading
    ) {
        Text(
            modifier = Modifier.padding(all = 4.dp),
            text = stringResource(id = R.string.deny),
            color = DenyRed,
            fontSize = 16.sp
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onClick = { onApproveClicked() },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(backgroundColor = ApprovalGreenBackground),
        enabled = !isLoading
    ) {
        Text(
            modifier = Modifier.padding(all = 6.dp),
            text = stringResource(id = R.string.approve),
            color = ApprovalGreen,
            fontSize = 24.sp
        )
    }
}

@Composable
fun ConfirmDispositionAlertDialog(
    dialogTitle: String,
    dialogText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        backgroundColor = UnfocusedGrey,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = dialogTitle,
                color = StrikeWhite,
                fontSize = 24.sp
            )
        },
        text = {
            Text(
                text = dialogText,
                color = StrikeWhite,
                fontSize = 18.sp
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(R.string.dismiss))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        }
    )
}

//region Preview

//This stateless composable can be useful for quick iteration on UI
// and using the preview to test
@Composable
fun StatelessApprovalDetailsScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ApprovalDetailsTopAppBar(
                title = stringResource(id = R.string.transfer_details),
                onAppBarIconClick = {},
                navigationIcon = Icons.Rounded.ArrowBackIos,
                navigationIconContentDes = stringResource(id = R.string.content_des_back_icon)
            )
        },
        content = {
            ApprovalDetails(
                onApproveClicked = { strikeLog(message = "Approve clicked") },
                onDenyClicked = { strikeLog(message = "Deny clicked") },
                timeRemainingInSeconds = 1000,
                isLoading = false
            )
        }
    )
}

@Preview(showBackground = true, name = "StatelessApprovalDetailsScreen")
@Composable
fun ApprovalDetailsScreenPreview() {
    StatelessApprovalDetailsScreen()
}
//endregion