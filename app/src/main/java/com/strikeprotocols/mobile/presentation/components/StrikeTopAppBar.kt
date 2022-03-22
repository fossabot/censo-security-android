package com.strikeprotocols.mobile.presentation.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.strikeprotocols.mobile.ui.theme.HeaderBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun StrikeTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String
) {
    TopAppBar(
        title = { Text(text = title) },
        backgroundColor = HeaderBlack,
        contentColor = StrikeWhite,
        navigationIcon = {
            IconButton(onClick = { onAppBarIconClick() }) {
                Icon(
                    navigationIcon,
                    navigationIconContentDes,
                    tint = StrikeWhite
                )
            }
        }
    )
}