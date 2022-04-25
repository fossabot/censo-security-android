package com.strikeprotocols.mobile.presentation.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun ApprovalRowTitleText(title: String) {
    Text(
        text = title,
        color = StrikeWhite,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}