package com.strikeprotocols.mobile.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.ui.theme.StrikePurple
import com.strikeprotocols.mobile.ui.theme.StrikeWhite
import com.strikeprotocols.mobile.ui.theme.UnfocusedGrey

@ExperimentalComposeUiApi
@Composable
fun SignInTextField(
    valueText: String,
    onValueChange: (newValue: String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    errorEnabled: Boolean = false,
    onDoneAction: () -> Unit = { },
    onPasswordClick: () -> Unit = { },
    passwordVisibility: Boolean = false,
    isPassword: Boolean = false,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = valueText,
            placeholder = {
                Text(text = placeholder, color = UnfocusedGrey)
            },
            textStyle = MaterialTheme.typography.body1,
            visualTransformation =
            if (passwordVisibility || !isPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = if (isPassword) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onDoneAction()
                }
            ),
            onValueChange = onValueChange,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = StrikePurple,
                unfocusedBorderColor = UnfocusedGrey,
                textColor = StrikeWhite
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
                                tint = UnfocusedGrey,
                                contentDescription = stringResource(contentDescription)
                            )
                        }
                    }
                } else {
                    null
                }
        )

        if(errorEnabled) {
            val errorStringId =
                if(isPassword) R.string.invalid_password_error else R.string.invalid_email_error

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(errorStringId),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

    }
}