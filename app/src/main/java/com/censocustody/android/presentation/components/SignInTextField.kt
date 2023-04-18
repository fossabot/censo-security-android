package com.censocustody.android.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.ui.theme.BorderGrey
import com.censocustody.android.ui.theme.ButtonRed
import com.censocustody.android.ui.theme.DarkGreyText

@ExperimentalComposeUiApi
@Composable
fun SignInTextField(
    modifier: Modifier = Modifier,
    valueText: String,
    placeholder: String,
    onValueChange: (newValue: String) -> Unit,
    keyboardType: KeyboardType,
    errorEnabled: Boolean = false,
    onDoneAction: () -> Unit = { },
    showDoneAction: Boolean = false,
    errorText: String = ""
) {
    Column {
        OutlinedTextField(
            singleLine = true,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            value = valueText,
            textStyle = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = if (showDoneAction) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    onDoneAction()
                },
                onDone = {
                    onDoneAction()
                }
            ),
            onValueChange = onValueChange,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = ButtonRed.copy(alpha = 0.5f),
                unfocusedBorderColor = BorderGrey,
                textColor = Color.Black,
                backgroundColor = Color.White
            ),
            placeholder = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = placeholder,
                    color = DarkGreyText,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            isError = errorEnabled
        )

        if (errorEnabled) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colors.error,
                modifier = modifier
            )
        }

    }
}