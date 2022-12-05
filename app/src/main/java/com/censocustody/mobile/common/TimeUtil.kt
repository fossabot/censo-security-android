package com.censocustody.mobile.common

import android.content.Context
import com.censocustody.mobile.R
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val iso8601FullPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx"
const val HOURS_IN_SECONDS = 3_600
const val MINUTES_IN_SECONDS = 60

fun generateFormattedTimestamp(): String {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(iso8601FullPattern)
    return now.format(formatter)
}

fun convertSecondsIntoCountdownText(context: Context, totalTimeInSeconds: Long?): String? {
    if(totalTimeInSeconds == null) return null

    if(totalTimeInSeconds <= 0) return context.getString(R.string.approval_expired)

    val hours = totalTimeInSeconds / HOURS_IN_SECONDS
    val minutes = totalTimeInSeconds / MINUTES_IN_SECONDS % 60
    val seconds = totalTimeInSeconds % 60

    return "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
}

fun convertSecondsIntoReadableText(totalTimeInMilliSeconds: Int, context: Context): String {
    val expirationTimeStringBuilder = StringBuilder().append("")

    val totalTimeInSeconds = totalTimeInMilliSeconds / 1000
    if (totalTimeInSeconds <= 0) return expirationTimeStringBuilder.toString()

    val hours: Int = totalTimeInSeconds / HOURS_IN_SECONDS
    val minutes: Int  = totalTimeInSeconds / MINUTES_IN_SECONDS % 60
    val seconds: Int  = totalTimeInSeconds % 60

    expirationTimeStringBuilder.append(
        if (hours > 0) "$hours ${context.getString(R.string.hours)}" else ""
    )
    expirationTimeStringBuilder.append(
        if (minutes > 0) " $minutes ${context.getString(R.string.minutes)}" else ""
    )

    val secondsText: String = if (seconds > 0) {
        val secondsTextStringBuilder = StringBuilder()

        val expirationTimeContainsHourOrMinutesText =
            expirationTimeStringBuilder.contains(context.getString(R.string.hours)) ||
                    expirationTimeStringBuilder.contains(context.getString(R.string.minutes))

        if (expirationTimeContainsHourOrMinutesText) {
            secondsTextStringBuilder.append(System.getProperty("line.separator"))
        }
        secondsTextStringBuilder.append("$seconds ${context.getString(R.string.seconds)}")
            .toString()
    } else {
        ""
    }

    expirationTimeStringBuilder.append(
        secondsText
    )

    return expirationTimeStringBuilder.toString()
}

fun calculateSecondsLeftUntilCountdownIsOver(submitDate: String?, totalTimeInSeconds: Int?) : Long? {
    if(totalTimeInSeconds == null) return null

    if(totalTimeInSeconds <= 0 || submitDate == null) return 0

    //Convert the submit date into epoch seconds
    // and get the end date (in epoch seconds) of the countdown
    val submitDateInSeconds = submitDate.formatISO8601IntoSeconds()
    val endDateInSeconds = submitDateInSeconds + totalTimeInSeconds

    //Get the current time in milliseconds and then divide to get the time in seconds
    val currentDateInSeconds = Calendar.getInstance().timeInMillis / 1000

    //Calculate the seconds left until the end date is reached, this is our true countdown
    val secondsLeftUntilEndDateIsReached = endDateInSeconds - currentDateInSeconds

    return if (secondsLeftUntilEndDateIsReached > 0) {
        secondsLeftUntilEndDateIsReached
    } else {
        0
    }
}

fun String.formatISO8601IntoSeconds() : Long {
    return try {
        val iso8601Formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val offsetDateTime = OffsetDateTime.parse(this, iso8601Formatter)

        offsetDateTime.toEpochSecond()
    } catch (e: Exception) {
        0
    }
}