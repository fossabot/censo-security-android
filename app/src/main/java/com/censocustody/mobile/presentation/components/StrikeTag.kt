package com.strikeprotocols.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getFullDestinationName
import com.strikeprotocols.mobile.ui.theme.SectionBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun StrikeTag(
    modifier: Modifier = Modifier,
    text: String,
    paddingValues: PaddingValues,
    annotatedText: AnnotatedString? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if(annotatedText != null) {
            Text(
                text = annotatedText,
                color = StrikeWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(paddingValues = paddingValues)
            )
        } else {
            Text(
                text = text,
                color = StrikeWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(paddingValues = paddingValues)
            )
        }
    }
}

@Composable
fun StrikeTagRow(
    text1: String,
    text2: String,
    arrowForward: Boolean
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        val strikeTag1Ref = createRef()
        val strikeTag2Ref = createRef()
        val iconRef = createRef()

        val strikeTagPaddingValues =
            PaddingValues(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
        StrikeTag(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = SectionBlack)
                .constrainAs(strikeTag1Ref) {
                    start.linkTo(parent.start)
                    end.linkTo(iconRef.start, 2.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = text1,
            paddingValues = strikeTagPaddingValues
        )

        val iconArrow = if (arrowForward) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack
        val iconContentDescription =
            if (arrowForward) stringResource(R.string.arrow_forward_content_des)
            else stringResource(R.string.arrow_back_content_des)
        Icon(
            modifier = Modifier
                .size(20.dp)
                .constrainAs(iconRef) {
                    centerHorizontallyTo(parent)
                    centerVerticallyTo(parent)
                },
            imageVector = iconArrow,
            tint = StrikeWhite,
            contentDescription = iconContentDescription
        )

        StrikeTag(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = SectionBlack)
                .constrainAs(strikeTag2Ref) {
                    start.linkTo(iconRef.end, 2.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = text2,
            paddingValues = strikeTagPaddingValues
        )
    }
}

@Composable
fun StrikeTagLabeledRow(
    text1: String,
    text2: String,
    subText2: String = "",
    label1: String,
    label2: String,
    arrowForward: Boolean
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        val strikeTag1Ref = createRef()
        val strikeTag2Ref = createRef()

        val label1Ref = createRef()
        val label2Ref = createRef()

        val iconRef = createRef()

        val strikeTagPaddingValues =
            PaddingValues(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
        StrikeTag(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = SectionBlack)
                .constrainAs(strikeTag1Ref) {
                    start.linkTo(parent.start)
                    end.linkTo(iconRef.start, 2.dp)
                    top.linkTo(label1Ref.bottom)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = text1,
            paddingValues = strikeTagPaddingValues
        )

        Text(
            modifier = Modifier
                .constrainAs(label1Ref) {
                    start.linkTo(parent.start)
                    end.linkTo(iconRef.start, 2.dp)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                }.padding(bottom = 20.dp),
            text = label1,
            textAlign = TextAlign.Center,
            color = StrikeWhite
        )

        Text(
            modifier = Modifier
                .constrainAs(label2Ref) {
                    start.linkTo(iconRef.end)
                    end.linkTo(parent.end, 2.dp)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                }.padding(bottom = 20.dp),
            text = label2,
            textAlign = TextAlign.Center,
            color = StrikeWhite
        )

        val iconArrow = if (arrowForward) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack
        val iconContentDescription =
            if (arrowForward) stringResource(R.string.arrow_forward_content_des)
            else stringResource(R.string.arrow_back_content_des)
        Icon(
            modifier = Modifier
                .size(20.dp)
                .constrainAs(iconRef) {
                    centerHorizontallyTo(parent)
                    centerVerticallyTo(strikeTag1Ref)
                },
            imageVector = iconArrow,
            tint = StrikeWhite,
            contentDescription = iconContentDescription
        )

        StrikeTag(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = SectionBlack)
                .constrainAs(strikeTag2Ref) {
                    start.linkTo(iconRef.end, 2.dp)
                    end.linkTo(parent.end)
                    top.linkTo(label2Ref.bottom)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = "",
            annotatedText = getFullDestinationName(initialValue = text2, subText = subText2),
            paddingValues = strikeTagPaddingValues
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StrikeTagPreview() {
    StrikeTag(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = SectionBlack),
        text = "Tag Example",
        paddingValues = PaddingValues(8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun StrikeTagRowPreview() {
    StrikeTagRow(
        text1 = "Sample DApp", text2 = "Main", arrowForward = false
    )
}