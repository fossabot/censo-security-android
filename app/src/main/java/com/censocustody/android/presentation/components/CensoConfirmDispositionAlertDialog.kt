package com.censocustody.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.censocustody.android.R
import com.censocustody.android.ui.theme.*

@Composable
fun CensoConfirmDispositionAlertDialog(
    dialogMessages: Pair<String, String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val upperInteractionSource = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .padding(4.dp)
            .clickable(indication = null, interactionSource = upperInteractionSource) {  },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val innerInteractionSource = remember { MutableInteractionSource() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(color = Color.Transparent)
                .border(
                    width = 1.5.dp,
                    shape = RoundedCornerShape(16.dp),
                    color = UnfocusedGrey.copy(alpha = 0.50f),
                )
                .zIndex(5.0f)
                .clickable(indication = null, interactionSource = innerInteractionSource) {  },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DialogHeaderBlack,
                        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                    )
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .align(Alignment.Center),
                    text = stringResource(R.string.are_you_sure),
                    fontWeight = FontWeight.SemiBold,
                    color = CensoWhite,
                    fontSize = 24.sp
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onClick = onDismiss
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_dialog),
                        tint = CensoWhite
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(
                    color = DialogMainBackground,
                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                )
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    text = dialogMessages.first,
                    color = CensoWhite,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    text = dialogMessages.second,
                    color = CensoWhite,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(0.20f))
                    Button(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .weight(1.35f).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = CancelButtonGrey
                        ),
                        onClick = onDismiss
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 2.dp),
                            fontSize = 18.sp,
                            text = stringResource(R.string.dismiss),
                            color = CensoWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.20f))
                    Button(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .weight(1.35f).fillMaxWidth(),
                        onClick = onConfirm,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 2.dp),
                            text = stringResource(id = R.string.confirm),
                            fontSize = 18.sp,
                            color = CensoWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.20f))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}