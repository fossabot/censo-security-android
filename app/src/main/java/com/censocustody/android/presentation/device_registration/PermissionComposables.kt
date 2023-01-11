package com.censocustody.android.presentation.device_registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.censocustody.android.R
import com.censocustody.android.common.CensoButton
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.ui.theme.UnfocusedGrey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState

@ExperimentalPermissionsApi
@Composable
fun Permission(
    permission: String,
    rationale: String,
    permissionNotAvailableContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val permissionState = rememberPermissionState(permission)
    PermissionRequired(
        permissionState = permissionState,
        permissionNotGrantedContent = {
            Rationale(
                text = rationale,
                onRequestPermission = { permissionState.launchPermissionRequest() }
            )
        },
        permissionNotAvailableContent = permissionNotAvailableContent,
        content = content
    )
}

@Composable
private fun Rationale(
    text: String,
    onRequestPermission: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
                .border(width = 1.5.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                .background(color = Color.Black)
                .zIndex(2.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(R.string.camera_permission_request),
                color = CensoWhite,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = text,
                color = CensoWhite,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            CensoButton(
                onClick = onRequestPermission,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.ok),
                    color = CensoWhite,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}