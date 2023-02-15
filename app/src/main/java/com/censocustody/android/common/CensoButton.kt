package com.censocustody.android.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.censocustody.android.ui.theme.ButtonRed

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
        shape = RoundedCornerShape(4.dp),
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ButtonRed,
            disabledBackgroundColor = ButtonRed,
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        content()
    }
}