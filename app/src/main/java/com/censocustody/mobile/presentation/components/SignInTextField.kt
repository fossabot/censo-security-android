package com.censocustody.mobile.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.censocustody.mobile.R
import com.censocustody.mobile.ui.theme.CensoPurple

@ExperimentalComposeUiApi
@Composable
fun SignInTextField(
    modifier: Modifier = Modifier,
    valueText: String,
    onValueChange: (newValue: String) -> Unit,
    keyboardType: KeyboardType,
    errorEnabled: Boolean = false,
    onDoneAction: () -> Unit = { },
    onPasswordClick: () -> Unit = { },
    passwordVisibility: Boolean = false,
    isPassword: Boolean = false,
    showDoneAction: Boolean = false,
    errorText: String = ""
) {
    Column {
        OutlinedTextField(
            singleLine = true,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            value = valueText,
            textStyle = MaterialTheme.typography.body1,
            visualTransformation =
            if (passwordVisibility || !isPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
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
                focusedBorderColor = CensoPurple,
                unfocusedBorderColor = Color.Transparent,
                textColor = Color.Black,
                backgroundColor = Color.White
            ),
            isError = errorEnabled,
            trailingIcon =
            if (isPassword) {
                {
                    val image =
                        if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val contentDescription =
                        if (passwordVisibility) R.string.hide_password_content_description else R.string.show_password_content_description
                    IconButton(onClick = onPasswordClick) {
                        Icon(
                            imageVector = image,
                            tint = Color.Black,
                            contentDescription = stringResource(contentDescription)
                        )
                    }
                }
            } else {
                null
            }
        )

        if (errorEnabled) {
            var errorMessage = errorText

            if (errorMessage.isEmpty()) {
                errorMessage =
                    if (isPassword) {
                        stringResource(R.string.invalid_password_error)
                    } else {
                        stringResource(R.string.invalid_email_error)
                    }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                modifier = modifier
            )
        }

    }
}