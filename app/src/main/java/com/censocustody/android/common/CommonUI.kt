package com.censocustody.android.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.ui.theme.*

@Composable
fun BackgroundUI() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = BackgroundGrey)
    )
}

@Composable
fun AllSetUI(
    allSetState: Resource<Unit>,
    retry: () -> Unit,
    onNavigate: () -> Unit,
    loadingText: String? = null
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        when (allSetState) {
            is Resource.Success, is Resource.Uninitialized -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = AllSetGreen, shape = CircleShape)
                            .padding(15.dp)
                            .layout() { measurable, constraints ->
                                // Measure the composable
                                val placeable = measurable.measure(constraints)

                                //get the current max dimension to assign width=height
                                val currentHeight = placeable.height
                                var heightCircle = currentHeight
                                if (placeable.width > heightCircle)
                                    heightCircle = placeable.width

                                //assign the dimension and the center position
                                layout(heightCircle, heightCircle) {
                                    // Where the composable gets placed
                                    placeable.placeRelative(0, (heightCircle - currentHeight) / 2)
                                }
                            }
                    ) {
                        Image(
                            modifier = Modifier.height(30.dp),
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "",
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.all_set),
                        color = TextBlack,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.23.sp
                    )
                }
                AuthFlowButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 56.dp),
                    text = stringResource(R.string.continue_to_censo),
                ) {
                    onNavigate()
                }
            }
            is Resource.Loading -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = loadingText ?: stringResource(R.string.registering_key_auth),
                            textAlign = TextAlign.Center,
                            color = TextBlack,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(36.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ButtonRed,
                            strokeWidth = 2.5.dp,
                        )
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
            is Resource.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = allSetState.censoError?.getErrorMessage(context)
                            ?: stringResource(R.string.something_went_wrong),
                        color = TextBlack,
                        fontSize = 18.sp,
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
            }
        }
    }
}

@Composable
fun AuthFlowButton(
    modifier: Modifier = Modifier,
    text: String,
    imageVector: ImageVector? = null,
    onClick: () -> Unit
) {
    CensoButton(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 12.dp),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = "",
                    tint = CensoWhite
                )
                Spacer(modifier = Modifier.width(24.dp))
            }
            Text(
                text = text,
                fontSize = 18.sp,
                color = CensoWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SmallAuthFlowButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    CensoButton(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = CensoWhite,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PreBiometryDialog(
    mainText: String,
    onAccept: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                modifier = Modifier.padding(horizontal = 16.dp),
                text = mainText,
                textAlign = TextAlign.Center,
                color = TextBlack,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(36.dp))
            CensoButton(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                onClick = onAccept,
            ) {
                Text(
                    text = stringResource(id = R.string.continue_text),
                    fontSize = 18.sp,
                    color = CensoWhite,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}