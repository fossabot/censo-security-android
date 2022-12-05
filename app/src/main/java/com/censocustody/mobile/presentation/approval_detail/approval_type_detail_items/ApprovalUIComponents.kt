package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.ui.theme.*

@Composable
fun ApprovalRowTitleText(title: String) {
    Text(
        text = title,
        color = StrikeWhite,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ApprovalSubtitle(text: String, fontSize: TextUnit = 16.sp) {
    Text(
        text = text,
        color = SubtitleGrey,
        textAlign = TextAlign.Center,
        fontSize = fontSize,
        letterSpacing = 0.23.sp
    )
}

@Composable
fun ApprovalInfoRow(
    backgroundColor: Color,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            color = StrikeWhite,
            fontSize = 16.sp,
            letterSpacing = 0.25.sp,
        )

        Text(
            modifier = Modifier
                .padding(end = 16.dp)
                .wrapContentHeight(),
            text = value,
            textAlign = TextAlign.Center,
            color = StrikeWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.25.sp
        )
    }
}

@Composable
fun AccountRow(
    title: String,
    value: String,
    titleColor: Color = StrikeWhite,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccountRowBackground)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            color = titleColor,
            fontSize = 18.sp,
            letterSpacing = 0.25.sp,
            fontWeight = FontWeight.W400
        )
        Text(
            modifier = Modifier
                .padding(end = 16.dp)
                .wrapContentHeight(),
            text = value,
            textAlign = TextAlign.Center,
            color = AccountTextGrey,
            fontSize = 16.sp,
            letterSpacing = 0.25.sp
        )
    }
}