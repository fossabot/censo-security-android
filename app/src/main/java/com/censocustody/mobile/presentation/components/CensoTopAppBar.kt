package com.censocustody.mobile.presentation.components

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
import com.censocustody.mobile.R
import com.censocustody.mobile.ui.theme.BackgroundBlack
import com.censocustody.mobile.ui.theme.CensoWhite


@Composable
fun SignInTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        contentColor = CensoWhite,
        elevation = AppBarDefaults.TopAppBarElevation,
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
                    tint = CensoWhite
                )
                Text(
                    text = title,
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp,
                    color = CensoWhite,
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
        contentColor = CensoWhite,
        elevation = AppBarDefaults.TopAppBarElevation,
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
                tint = CensoWhite
            )
            Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                color = CensoWhite,
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
    actions: @Composable RowScope.() -> Unit = {}
) {
    val appBarHorizontalPadding = 4.dp
    val titleIconModifier = Modifier
        .fillMaxHeight()
        .width(72.dp - appBarHorizontalPadding)

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = BackgroundBlack,
        contentColor = CensoWhite,
        elevation = AppBarDefaults.TopAppBarElevation,
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
                            tint = CensoWhite
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
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp,
                    color = CensoWhite,
                    lineHeight = 10.sp,
                    letterSpacing = 0.23.sp
                )
            }

        }
    }
}