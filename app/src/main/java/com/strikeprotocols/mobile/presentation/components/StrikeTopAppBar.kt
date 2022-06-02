package com.strikeprotocols.mobile.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.HeaderBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@SuppressLint("ComposableLambdaParameterNaming")
@Composable
fun StrikeTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String,
    actions: @Composable RowScope.() -> Unit = {},
    ) {
    TopAppBar(
        title = { Text(text = title) },
        backgroundColor = BackgroundBlack,
        contentColor = StrikeWhite,
        navigationIcon = {
            IconButton(onClick = { onAppBarIconClick() }) {
                Icon(
                    navigationIcon,
                    navigationIconContentDes,
                    tint = StrikeWhite
                )
            }
        },
        actions = actions
    )
}