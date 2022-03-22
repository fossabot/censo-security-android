package com.strikeprotocols.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.ui.theme.SectionBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun StrikeTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = SectionBlack)
            .padding(top = 12.dp, bottom = 12.dp, start = 44.dp, end = 44.dp)
    ) {
        Text(text, color = StrikeWhite, textAlign = TextAlign.Center)
    }
}