package com.censocustody.mobile.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.censocustody.mobile.ui.theme.ButtonBorder
import com.censocustody.mobile.ui.theme.CensoButtonBlue

@Composable
fun CensoButton(
    modifier: Modifier = Modifier,
    height: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(),
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable() () -> Unit
) {

    val buttonModifier = if (height != null) modifier.height(height) else modifier

    Button(
        modifier = buttonModifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(width = 0.5.dp, color = ButtonBorder),
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = CensoButtonBlue,
            disabledBackgroundColor = CensoButtonBlue,
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        content()
    }
}