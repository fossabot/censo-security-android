package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.common.CrashReportingUtil
import com.censocustody.android.presentation.approvals.approval_type_row_items.nameToInitials
import com.censocustody.android.ui.theme.*
import com.raygun.raygun4android.RaygunClient


@Composable
fun ApprovalRowTitleText(title: String) {
    Text(
        text = title,
        color = TextBlack,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ApprovalSubtitle(text: String, fontSize: TextUnit = 16.sp) {
    Text(
        text = text,
        color = DarkGreyText,
        textAlign = TextAlign.Center,
        fontSize = fontSize,
        letterSpacing = 0.23.sp
    )
}

@Composable
fun UserInfoRow(
    backgroundColor: Color,
    name: String,
    email: String,
    image: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        UserImage(image, name)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = name,
                color = TextBlack,
                fontSize = 18.sp,
                textAlign = TextAlign.Start,
                letterSpacing = 0.25.sp,
            )

            Text(
                text = email,
                textAlign = TextAlign.Start,
                color = TextBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.25.sp
            )
        }
    }
}

@Composable
fun UserImage(image: String?, name: String) {
    var bitmap: Bitmap? = null
    var haveValidBitmap = false
    if (!image.isNullOrEmpty()) {
        bitmap = image.toBitMap()

        if (bitmap != null) {
            haveValidBitmap = true
        }
    }

    if (haveValidBitmap) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "approver image",
            contentScale = ContentScale.Crop, // crop the image if it's not a square
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .size(56.dp)
                .clip(CircleShape) // clip to the circle shape
        )
    } else {
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clip(CircleShape)
                .background(color = CensoWhite)
                .size(56.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = name.nameToInitials(),
                fontSize = 18.sp,
                color = ButtonRed
            )
        }
    }
}

@Composable
fun UserRoleRow(
    backgroundColor: Color,
    name: String,
    email: String,
    role: String,
    image: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            UserImage(image, name)
            Column(
            ) {
                Text(
                    text = name,
                    color = TextBlack,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    letterSpacing = 0.25.sp,
                )

                Text(
                    text = email,
                    textAlign = TextAlign.Start,
                    color = TextBlack,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.25.sp
                )
            }
        }
        Text(
            modifier = Modifier
                .padding(end = 8.dp)
                .wrapContentHeight(),
            text = role,
            textAlign = TextAlign.End,
            color = TextBlack,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.25.sp
        )
    }
}

@Composable
fun ApprovalInfoRow(
    backgroundColor: Color,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            color = TextBlack,
            fontSize = 16.sp,
            letterSpacing = 0.25.sp,
        )

        Text(
            modifier = Modifier
                .padding(end = 16.dp)
                .wrapContentHeight(),
            text = value,
            textAlign = TextAlign.Center,
            color = TextBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.25.sp
        )
    }
}

fun String.toBitMap(): Bitmap? {
    return try {
        val encodeByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    } catch (e: Exception) {
        RaygunClient.send(
            e, listOf(
                CrashReportingUtil.IMAGE,
                CrashReportingUtil.MANUALLY_REPORTED_TAG
            )
        )
        null
    }
}

@Composable
fun AccountRow(
    title: String,
    value: String,
    titleColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundLight)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            color = titleColor,
            fontSize = 18.sp,
            letterSpacing = 0.25.sp,
            fontWeight = FontWeight.W400
        )
        Text(
            modifier = Modifier
                .padding(end = 16.dp)
                .wrapContentHeight(),
            text = value,
            textAlign = TextAlign.Center,
            color = TextBlack,
            fontSize = 16.sp,
            letterSpacing = 0.25.sp
        )
    }
}