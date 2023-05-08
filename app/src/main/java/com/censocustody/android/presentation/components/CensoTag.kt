package com.censocustody.android.presentation.components

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
import com.censocustody.android.R
import com.censocustody.android.ui.theme.*

@Composable
fun CensoTag(
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
                color = TextBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(paddingValues = paddingValues)
            )
        } else {
            Text(
                text = text,
                color = TextBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(paddingValues = paddingValues)
            )
        }
    }
}

@Composable
fun CensoTagRow(
    text1: String,
    text2: String,
    arrowForward: Boolean
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        val censoTag1Ref = createRef()
        val censoTag2Ref = createRef()
        val iconRef = createRef()

        val censoTagPaddingValues =
            PaddingValues(top = 10.dp, bottom = 10.dp, start = 12.dp, end = 12.dp)
        CensoTag(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color = BackgroundDark)
                .constrainAs(censoTag1Ref) {
                    start.linkTo(parent.start)
                    end.linkTo(iconRef.start, 2.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = text1,
            paddingValues = censoTagPaddingValues
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
            tint = DarkGreyText,
            contentDescription = iconContentDescription
        )

        CensoTag(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color = BackgroundDark)
                .constrainAs(censoTag2Ref) {
                    start.linkTo(iconRef.end, 2.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = text2,
            paddingValues = censoTagPaddingValues
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CensoTagPreview() {
    CensoTag(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = BackgroundGrey),
        text = "Tag Example",
        paddingValues = PaddingValues(8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CensoTagRowPreview() {
    CensoTagRow(
        text1 = "Sample DApp", text2 = "Main", arrowForward = false
    )
}