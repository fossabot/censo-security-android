package com.strikeprotocols.mobile.presentation.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.ui.theme.DetailInfoDarkBackground
import com.strikeprotocols.mobile.ui.theme.GreyText
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun AccountChangeItem(header: String, title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        ) {
        ApprovalContentHeader(header = header, topSpacing = 12, bottomSpacing = 32)
        AccountTitleSubtitle(title = title, subtitle = subtitle)
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
fun AccountTitleSubtitle(title: String, subtitle: String) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(color = DetailInfoDarkBackground)
        .padding(horizontal = 20.dp))
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, color = StrikeWhite, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = GreyText, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    androidx.compose.material.Surface(
        color = Color.Black
    ) {
        AccountChangeItem(
            header = "Adding Signer",
            title = "John Malkovich",
            subtitle = "john@hollywood.com"
        )
    }
}