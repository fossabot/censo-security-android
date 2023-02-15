package com.censocustody.android.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.ui.theme.BackgroundWhite
import com.censocustody.android.ui.theme.TextBlack
import com.censocustody.android.ui.theme.TopAppBarText


@Composable
fun SignInTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        contentColor = TextBlack,
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .clickable { onAppBarIconClick() }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(title.isNotEmpty()) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.back),
                    tint = TextBlack
                )
                Text(
                    text = title,
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp,
                    color = TextBlack,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
    }
}

@Composable
fun AuthTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        contentColor = TextBlack,
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .clickable { onAppBarIconClick() }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = stringResource(R.string.back),
                tint = TextBlack
            )
            Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                color = TextBlack,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@SuppressLint("ComposableLambdaParameterNaming")
@Composable
fun CenteredTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String,
    showNavIcon: Boolean = true,
    backgroundColor : Color = BackgroundWhite,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val appBarHorizontalPadding = 4.dp
    val titleIconModifier = Modifier
        .fillMaxHeight()
        .width(72.dp - appBarHorizontalPadding)

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = backgroundColor,
        contentColor = TextBlack,
        elevation = 0.dp,
    ) {
        Box(modifier = Modifier.height(56.dp)) {

            //Navigation Icon
            Row(
                titleIconModifier, verticalAlignment = Alignment.CenterVertically
            ) {
                if (showNavIcon) {
                    IconButton(onClick = { onAppBarIconClick() }) {
                        Icon(
                            navigationIcon,
                            navigationIconContentDes,
                            tint = TextBlack
                        )
                    }
                }
            }

            //actions
            Row(
                Modifier
                    .fillMaxHeight()
                    .align(alignment = Alignment.CenterEnd),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )

            //Title
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    text = title,
                    fontWeight = FontWeight.W500,
                    fontSize = 24.sp,
                    color = TopAppBarText,
                    lineHeight = 10.sp,
                    letterSpacing = 0.30.sp
                )
            }

        }
    }
}