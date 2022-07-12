package com.strikeprotocols.mobile.presentation.sign_in

import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipboardManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.IndexedPhraseWord
import com.strikeprotocols.mobile.presentation.sign_in.PhraseUICompanion.DISPLAY_RANGE_SET
import com.strikeprotocols.mobile.ui.theme.*


@Composable
fun PhraseBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.30f)
            .blur(250.dp)
            .background(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF996cfd),
                        2.50f to Color(0xFF7351BE),
                        5.0f to Color(0xFF4D367F),
                        7.5f to Color(0xFF261B3F),
                        10.0f to Color(0xFF000000),
                    )
                )
            )
    )
}

@Composable
fun EntryScreenPhraseUI(
    title: String,
    subtitle: String,
    buttonOneText: String,
    buttonTwoText: String,
    onPhraseFlowAction: (PhraseFlowAction) -> Unit,
    onExit: () -> Unit,
    creationFlow: Boolean
) {

    val context = LocalContext.current

    val matrix = ColorMatrix()
    matrix.setToSaturation(0F)

    BackHandler {
        onExit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(modifier = Modifier.align(Alignment.CenterEnd), onClick = onExit) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.exit_flow),
                    tint = StrikeWhite
                )
            }
        }
        Spacer(modifier = Modifier.height(56.dp))
        Image(
            painter = painterResource(R.drawable.ic_key_auth),
            contentDescription = "",
            colorFilter = ColorFilter.colorMatrix(matrix)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = title,
            color = StrikeWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(72.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = subtitle,
                color = StrikeWhite,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.23.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            AuthFlowButton(text = buttonOneText, textPadding = 1.dp) {
                if (creationFlow) {
                    onPhraseFlowAction(
                        PhraseFlowAction.ChangeCreationFlowStep(
                            KeyCreationFlowStep.COPY_PHRASE_STEP
                        )
                    )
                } else {
                    onPhraseFlowAction(
                        PhraseFlowAction.ChangeRecoveryFlowStep(
                            KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            AuthFlowButton(text = buttonTwoText, textPadding = 1.dp) {
                if (creationFlow) {
                    onPhraseFlowAction(PhraseFlowAction.LaunchManualKeyCreation)
                } else {
                    Toast.makeText(context, "Coming soon...", Toast.LENGTH_LONG).show()
                }
            }
        }
        Spacer(modifier = Modifier.height(56.dp))
    }
}

@Composable
fun CopyKeyUI(phrase: String, phraseCopied: Boolean, phraseSaved: Boolean, onNavigate: () -> Unit) {

    val context = LocalContext.current
    val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Column(
        modifier = Modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(R.string.copy_key_message),
                    color = StrikeWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.23.sp
                )
                Spacer(modifier = Modifier.height(36.dp))
                AuthFlowButton(
                    text = stringResource(R.string.copy_private_key),
                    imageVector = Icons.Outlined.ContentCopy
                ) {
                    val clip: ClipData =
                        ClipData.newPlainText(SignInViewModel.CLIPBOARD_LABEL_PHRASE, phrase)
                    clipboard.setPrimaryClip(clip)
                    onNavigate()
                }
                Spacer(modifier = Modifier.height(56.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (phraseCopied) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "",
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.copied_to_clipboard),
                            color = StrikeWhite,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.23.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.paste_your_key),
                            color = StrikeWhite,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.23.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(56.dp))
                    if (phraseSaved) {
                        AuthFlowButton(
                            modifier = Modifier
                                .height(height = 82.dp)
                                .padding(bottom = 24.dp),
                            text = "I saved the phrase ->"
                        ) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                clipboard.clearPrimaryClip()
                            } else {
                                val clip: ClipData =
                                    ClipData.newPlainText(
                                        SignInViewModel.CLIPBOARD_LABEL_PHRASE,
                                        ""
                                    )
                                clipboard.setPrimaryClip(clip)
                            }
                            onNavigate()
                        }
                    } else {
                        Spacer(modifier = Modifier.height(106.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun ConfirmKeyUI(
    errorEnabled: Boolean = false,
    pastedPhrase: String,
    verifyPastedPhrase: (String) -> Unit,
    onNavigate: () -> Unit,
    header: String,
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.clickable { onNavigate() },
            text = header,
            color = StrikeWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = title,
            color = StrikeWhite,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = StrikeWhite,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(44.dp))
        OutlinedTextField(
            enabled = true,
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .background(color = Color.Black)
                .fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (errorEnabled) Color.Red else StrikePurple,
                unfocusedBorderColor = if (errorEnabled) Color.Red else GreyOutline,
                cursorColor = Color.Transparent,
                textColor = if (errorEnabled) Color.Red else StrikeWhite,
                errorBorderColor = Color.Red,
            ),
            value = pastedPhrase,
            onValueChange = {
                if (it != pastedPhrase) {
                    verifyPastedPhrase(it)
                }
            }
        )
    }
}

@Composable
fun AllSetUI(onNavigate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(color = AllSetGreen, shape = CircleShape)
                    .padding(15.dp)
                    .layout() { measurable, constraints ->
                        // Measure the composable
                        val placeable = measurable.measure(constraints)

                        //get the current max dimension to assign width=height
                        val currentHeight = placeable.height
                        var heightCircle = currentHeight
                        if (placeable.width > heightCircle)
                            heightCircle = placeable.width

                        //assign the dimension and the center position
                        layout(heightCircle, heightCircle) {
                            // Where the composable gets placed
                            placeable.placeRelative(0, (heightCircle - currentHeight) / 2)
                        }
                    }
            ) {
                Image(
                    modifier = Modifier.height(30.dp),
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "",
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.all_set),
                color = StrikeWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.23.sp
            )
        }
        AuthFlowButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            text = stringResource(R.string.continue_to_strike),
            textPadding = 4.dp
        ) {
            onNavigate()
        }
    }
}

@Composable
fun AuthFlowButton(
    modifier: Modifier = Modifier,
    textPadding: Dp = 0.dp,
    text: String,
    imageVector: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        onClick = onClick,
    ) {
        Row() {
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = "",
                    tint = StrikeWhite
                )
                Spacer(modifier = Modifier.width(24.dp))
            }
            Text(
                modifier = Modifier.padding(vertical = textPadding),
                text = text,
                fontSize = 18.sp,
                color = StrikeWhite
            )
        }
    }
}

@Composable
fun PhraseWords(
    phraseWords: List<IndexedPhraseWord> = emptyList()
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 48.dp)
            .border(width = 1.dp, color = UnfocusedGrey)
            .background(color = Color.Black.copy(alpha = 0.25f))
            .zIndex(2.5f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (phraseWords.isEmpty()) {
            Text(
                text = stringResource(R.string.phrase_words_empty),
                color = StrikeWhite,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
        } else {
            for ((localIndex, indexWord) in phraseWords.withIndex()) {
                //First Spacer
                if (localIndex == PhraseUICompanion.FIRST_SPACER_INDEX) {
                    Spacer(
                        modifier = Modifier.height(
                            dimensionResource(R.dimen.medium_word_spacer)
                        )
                    )
                }

                //Word Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 44.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getDisplayIndex(indexWord.wordIndex),
                        color = GreyText,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(34.dp))
                    Text(
                        text = indexWord.wordValue,
                        color = StrikeWhite,
                        fontSize = 28.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                //Row Spacers
                if (localIndex != PhraseUICompanion.LAST_SPACER_INDEX) {
                    Spacer(
                        modifier = Modifier.height(
                            dimensionResource(R.dimen.large_word_spacer)
                        )
                    )
                }

                //Last Spacer
                if (localIndex == PhraseUICompanion.LAST_SPACER_INDEX) {
                    Spacer(
                        modifier = Modifier.height(
                            dimensionResource(R.dimen.medium_word_spacer)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WriteWordUI(
    phrase: String,
    index: Int,
    onPhraseFlowAction: (phraseFlowAction: PhraseFlowAction) -> Unit
) {

    val context = LocalContext.current

    val splitWords = phrase.split(" ")
    val wordsToShow = mutableListOf<IndexedPhraseWord>()

    if (splitWords.size >= index + PhraseUICompanion.DISPLAY_RANGE_SET) {
        for ((wordIndex, word) in splitWords.withIndex()) {
            if (wordIndex in index..index + PhraseUICompanion.DISPLAY_RANGE_SET) {
                wordsToShow.add(
                    IndexedPhraseWord(
                        wordIndex = wordIndex + PhraseUICompanion.OFFSET_INDEX_ZERO,
                        wordValue = word
                    )
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .weight(1.0f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //region Title + Sub-title
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Text(
                text = stringResource(id = R.string.write_each_word_down),
                color = StrikeWhite,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(
                modifier = Modifier.height(12.dp)
            )
            Text(
                text = stringResource(
                    id = R.string.showing_of_words,
                    (index + 1).toString(),
                    (index + 1 + DISPLAY_RANGE_SET).toString()
                ),
                color = GreyText,
                fontSize = 18.sp
            )
            Spacer(
                modifier = Modifier.height(48.dp)
            )
            //endregion

            //Phrase words
            PhraseWords(
                phraseWords = wordsToShow
            )

            //region Buttons
            Spacer(
                modifier = Modifier.height(15.dp)
            )

        }


        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            //Buttons for displaying previous/next words
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            onPhraseFlowAction(PhraseFlowAction.WordIndexChanged(increasing = false))
                        }
                        .padding(end = 16.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Filled.NavigateBefore,
                        contentDescription = stringResource(R.string.previous_icon_content_desc),
                        tint = StrikeWhite
                    )
                    Text(
                        text = stringResource(R.string.previous),
                        color = StrikeWhite,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            onPhraseFlowAction(PhraseFlowAction.WordIndexChanged(increasing = true))
                        }
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.next),
                        color = StrikeWhite,
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Filled.NavigateNext,
                        contentDescription = stringResource(R.string.next_icon_content_desc),
                        tint = StrikeWhite
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            //Button for submitting
            Box(
                modifier = Modifier.height(72.dp),
                contentAlignment = Alignment.Center
            ) {
                if (index == SignInViewModel.LAST_WORD_RANGE_SET_INDEX) {
                    AuthFlowButton(
                        text = stringResource(R.string.saved_the_phrase),
                        modifier = Modifier.padding(horizontal = 44.dp)
                    ) {
                        Toast.makeText(context, "Coming soon...", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Spacer(modifier = Modifier.height(56.dp))
                }

            }
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            //endregion
        }
    }
}

fun getDisplayIndex(wordIndex: Int) =
    if (wordIndex < PhraseUICompanion.DOUBLE_DIGIT_INDEX) {
        " $wordIndex"
    } else {
        wordIndex.toString()
    }

object PhraseUICompanion {
    const val FIRST_SPACER_INDEX = 0
    const val LAST_SPACER_INDEX = 3

    const val DOUBLE_DIGIT_INDEX = 10

    const val DISPLAY_RANGE_SET = 3
    const val OFFSET_INDEX_ZERO = 1
}
