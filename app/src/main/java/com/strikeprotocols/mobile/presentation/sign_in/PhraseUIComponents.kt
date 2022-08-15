package com.strikeprotocols.mobile.presentation.sign_in

import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipboardManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.TextFieldDecorationBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ContentCopy
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.IndexedPhraseWord
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.sign_in.PhraseUICompanion.DISPLAY_RANGE_SET
import com.strikeprotocols.mobile.ui.theme.*

object PhraseUICompanion {
    const val FIRST_SPACER_INDEX = 0
    const val LAST_SPACER_INDEX = 3

    const val DOUBLE_DIGIT_INDEX = 10

    const val DISPLAY_RANGE_SET = 3
    const val OFFSET_INDEX_ZERO = 1
}

@Composable
fun PhraseBackground() {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.50f)
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
        else ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.40f)
                    .blur(250.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF996cfd),
                                10.0f to Color(0xFF000000)
                            )
                        )
                    )
            )
    }
}

@Composable
fun EntryScreenPhraseUI(
    title: String,
    subtitle: String,
    buttonOneText: String,
    buttonTwoText: String,
    onPhraseFlowAction: (PhraseFlowAction) -> Unit,
    onNavigate: () -> Unit,
    onExit: () -> Unit,
    creationFlow: Boolean
) {

    val matrix = ColorMatrix()
    matrix.setToSaturation(0F)

    BackHandler {
        onExit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.weight(0.25f))
        Box(modifier = Modifier
            .fillMaxWidth()) {
            IconButton(modifier = Modifier.align(Alignment.CenterStart), onClick = onExit) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(id = R.string.content_des_account_icon),
                    tint = StrikeWhite
                )
            }
        }
        Spacer(modifier = Modifier.weight(2.5f))
        Image(
            painter = painterResource(R.drawable.ic_key_auth),
            contentDescription = stringResource(R.string.key_icon_content_desc),
            colorFilter = ColorFilter.colorMatrix(matrix)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            text = title,
            color = StrikeWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.weight(0.8f))
        Text(
            text = subtitle,
            color = StrikeWhite,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.weight(0.8f))
        AuthFlowButton(
            text = buttonOneText,
            textPadding = 1.dp
        ) {
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
        Spacer(modifier = Modifier.weight(0.25f))
        AuthFlowButton(
            text = buttonTwoText,
            textPadding = 1.dp
        ) {
            if (creationFlow) {
                onPhraseFlowAction(PhraseFlowAction.LaunchManualKeyCreation)
            } else {
                onPhraseFlowAction(
                    PhraseFlowAction.ChangeRecoveryFlowStep(
                        KeyRecoveryFlowStep.VERIFY_WORDS_STEP
                    )
                )
                onNavigate()
            }
        }
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
fun CopyKeyUI(phrase: String, phraseCopied: Boolean, phraseSaved: Boolean, onNavigate: () -> Unit) {

    val context = LocalContext.current
    val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Column(
        modifier = Modifier
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(modifier = Modifier.weight(3.0f))
                Text(
                    text = stringResource(R.string.copy_key_message),
                    color = StrikeWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.23.sp
                )
                Spacer(modifier = Modifier.weight(0.75f))
                AuthFlowButton(
                    text = stringResource(R.string.copy_private_key),
                    imageVector = Icons.Outlined.ContentCopy
                ) {
                    val clip: ClipData =
                        ClipData.newPlainText(SignInViewModel.CLIPBOARD_LABEL_PHRASE, phrase)
                    clipboard.setPrimaryClip(clip)
                    onNavigate()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
        ) {
            if (phraseCopied) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.weight(0.5f))
                    Image(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "",
                    )
                    Spacer(modifier = Modifier.weight(0.5f))
                    Text(
                        text = stringResource(R.string.copied_to_clipboard),
                        color = StrikeWhite,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.23.sp
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    if (phraseSaved) {
                        AuthFlowButton(
                            modifier = Modifier
                                .height(height = 116.dp)
                                .padding(bottom = 24.dp),
                            text = stringResource(R.string.i_saved_key)
                        ) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                try {
                                    if (clipboard.hasPrimaryClip()) {
                                        clipboard.clearPrimaryClip()
                                    } else {
                                        val clip: ClipData =
                                            ClipData.newPlainText(
                                                SignInViewModel.CLIPBOARD_LABEL_PHRASE,
                                                ""
                                            )
                                        clipboard.setPrimaryClip(clip)
                                    }
                                } catch (e: Exception) {

                                    val clip: ClipData =
                                        ClipData.newPlainText(
                                            SignInViewModel.CLIPBOARD_LABEL_PHRASE,
                                            ""
                                        )
                                    clipboard.setPrimaryClip(clip)
                                }
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
    header: String,
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = header,
            color = StrikeWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        if (title.isNotEmpty()) {
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = title,
                color = StrikeWhite,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.23.sp
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = message,
            color = StrikeWhite,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(50.dp))
        OutlinedTextField(
            enabled = true,
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .background(color = Color.Black, shape = RoundedCornerShape(8.dp))
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
fun AllSetUI(
    allSetState: Resource<WalletSigner?>,
    retry: () -> Unit,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        when (allSetState) {
            is Resource.Success, is Resource.Uninitialized -> {
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
            is Resource.Loading -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(width = 1.5.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                            .background(color = Color.Black)
                            .zIndex(2.5f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(36.dp))
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = stringResource(R.string.registering_key_auth),
                            textAlign = TextAlign.Center,
                            color = StrikeWhite,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(36.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = StrikeWhite,
                            strokeWidth = 2.5.dp,
                        )
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
            is Resource.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = allSetState.strikeError?.getErrorMessage(context) ?: stringResource(R.string.something_went_wrong),
                        color = StrikeWhite,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.23.sp,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SmallAuthFlowButton(
                        modifier = Modifier.wrapContentWidth(),
                        text = stringResource(R.string.retry),
                        textPadding = 4.dp
                    ) {
                        retry()
                    }
                }
            }
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                color = StrikeWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SmallAuthFlowButton(
    modifier: Modifier = Modifier,
    textPadding: Dp = 0.dp,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(vertical = textPadding),
                text = text,
                fontSize = 18.sp,
                color = StrikeWhite,
                textAlign = TextAlign.Center
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
    onPhraseFlowAction: (phraseFlowAction: PhraseFlowAction) -> Unit,
    onNavigate: () -> Unit
) {

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
                        onPhraseFlowAction(PhraseFlowAction.ChangeCreationFlowStep(KeyCreationFlowStep.VERIFY_WORDS_STEP))
                        onNavigate()
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

@Composable
fun VerifyPhraseWordUI(
    wordIndex: Int,
    value: String,
    onValueChanged: (String) -> Unit,
    onSubmitWord: (String) -> Unit,
    onPreviousWordNavigate: () -> Unit,
    onNextWordNavigate: () -> Unit,
    isCreationFlow: Boolean,
    errorEnabled: Boolean
) {
    val isFirstWord = wordIndex == ConfirmPhraseWordsState.PHRASE_WORD_FIRST_INDEX
    val isLastWord = wordIndex == ConfirmPhraseWordsState.PHRASE_WORD_SECOND_TO_LAST_INDEX

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.weight(0.1f))
        Text(
            modifier = Modifier.padding(horizontal = 36.dp),
            text = stringResource(R.string.enter_each_word_to_verify),
            color = SubtitleLightGrey,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
        Spacer(modifier = Modifier.weight(0.05f))
        Text(
            text = stringResource(R.string.enter_word_number),
            color = StrikeWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
        Text(
            text = (wordIndex + 1).toString(),
            color = StrikeWhite,
            fontSize = 76.sp
        )
        Spacer(modifier = Modifier.weight(0.1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (errorEnabled) {
                    Text(
                        modifier = Modifier
                            .background(GreyText.copy(alpha = 0.15f))
                            .fillMaxWidth(),
                        text = stringResource(R.string.that_word_is_not_correct),
                        textAlign = TextAlign.Center,
                        color = ErrorRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                VerifyWordTextField(
                    text = value,
                    onTextChange = onValueChanged,
                    errorEnabled = errorEnabled,
                    onSubmitWord = onSubmitWord
                )

                if (!isCreationFlow) {
                    WordNavigationButtons(
                        value = value,
                        isFirstWord = isFirstWord,
                        isLastWord = isLastWord,
                        onPreviousWordNavigate = onPreviousWordNavigate,
                        onNextWordNavigate = onNextWordNavigate
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.45f))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VerifyWordTextField(
    text: String,
    onTextChange: (String) -> Unit,
    errorEnabled: Boolean,
    onSubmitWord: (String) -> Unit
) {
    val singleLine = true
    val enabled = true
    val interactionSource = remember { MutableInteractionSource() }
    val errorMessage = stringResource(id = R.string.do_not_leave_text_field_empty)

    BasicTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (errorEnabled) ErrorBorderRed else VerifyWordsBorder,
                shape = RoundedCornerShape(8.dp)
            ),
        value = text,
        onValueChange = onTextChange,
        textStyle = LocalTextStyle.current.copy(
            color = StrikeWhite,
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            letterSpacing = 0.75.sp,
            fontWeight = FontWeight.Bold
        ),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onNext = { onSubmitWord(errorMessage) }
        ),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(StrikePurple),
        singleLine = singleLine
    ) { innerTextField ->

        TextFieldDecorationBox(
            value = text,
            innerTextField = innerTextField,
            enabled = enabled,
            singleLine = singleLine,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(vertical = 8.dp)
        )
    }
}

@Composable
private fun WordNavigationButtons(
    value: String,
    onPreviousWordNavigate: () -> Unit,
    onNextWordNavigate: () -> Unit,
    isFirstWord: Boolean,
    isLastWord: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        //Previous Button
        val previousButtonVisibility = if (isFirstWord) 0f else 1f
        TextButton(
            modifier = Modifier
                .background(Color.Transparent)
                .padding(0.dp)
                .alpha(previousButtonVisibility),
            contentPadding = PaddingValues(0.dp),
            onClick = onPreviousWordNavigate
        ) {
            Icon(
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp),
                imageVector = Icons.Filled.NavigateBefore,
                contentDescription = stringResource(R.string.previous_icon_content_desc),
                tint = StrikeWhite
            )
            Text(
                text = stringResource(R.string.previous),
                color = StrikeWhite,
                fontSize = 16.sp
            )
        }

        //Next Button
        val nextButtonText = getNextButtonText(lastWord = isLastWord, LocalContext.current)
        val nextButtonContentDes = getNextButtonContentDescription(lastWord = isLastWord, LocalContext.current)
        val nextButtonEnabled = value.isNotEmpty() && value.isNotBlank()
        TextButton(
            modifier = Modifier
                .background(Color.Transparent)
                .padding(0.dp),
            contentPadding = PaddingValues(0.dp),
            enabled = nextButtonEnabled,
            onClick = onNextWordNavigate
        ) {
            Text(
                text = nextButtonText,
                color = if (nextButtonEnabled) StrikeWhite else GreyText,
                fontSize = 16.sp
            )
            Icon(
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp),
                imageVector = Icons.Filled.NavigateNext,
                contentDescription = nextButtonContentDes,
                tint = if (nextButtonEnabled) StrikeWhite else GreyText
            )
        }
    }
}

private fun getNextButtonText(lastWord: Boolean, context: Context) =
    if (lastWord) context.getString(R.string.finish)
    else context.getString(R.string.next)

private fun getNextButtonContentDescription(lastWord: Boolean, context: Context) =
    if (lastWord) context.getString(R.string.finish_icon_content_desc)
    else context.getString(R.string.next_icon_content_desc)