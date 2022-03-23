package com.strikeprotocols.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun StrikeTag(
    text: String,
    paddingValues: PaddingValues,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
    ) {
        Text(
            text = text,
            color = StrikeWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(paddingValues = paddingValues)
        )
    }
}