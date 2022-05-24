package com.strikeprotocols.mobile.common

import android.content.Context
import com.strikeprotocols.mobile.R
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun convertSecondsIntoCountdownText(context: Context, totalTimeInSeconds: Long): String {
    if(totalTimeInSeconds <= 0) return context.getString(R.string.approval_expired)

    val days = totalTimeInSeconds / DAYS_IN_SECONDS
    val hours = totalTimeInSeconds / HOURS_IN_SECONDS % HOURS_IN_DAY
    val minutes = totalTimeInSeconds / MINUTES_IN_SECONDS % 60
    val seconds = totalTimeInSeconds % 60

    return "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
}

fun String.formatISO8601IntoDisplayText(context: Context): String {
    if (this.isEmpty()) {
        return context.getString(R.string.not_applicable)
    }

    return try {
        val iso8601Formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val offsetDateTime = OffsetDateTime.parse(this, iso8601Formatter)

        val dateTimeFormatter =
            DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm:ss a", Locale.getDefault())

        offsetDateTime.format(dateTimeFormatter)
    } catch (e: Exception) {
        context.getString(R.string.not_applicable)
    }
}

fun convertSecondsIntoReadableText(totalTimeInSeconds: Int, context: Context): String {
    if (totalTimeInSeconds <= 0) return ""

    val days = totalTimeInSeconds / DAYS_IN_SECONDS
    val hours = totalTimeInSeconds / HOURS_IN_SECONDS % HOURS_IN_DAY
    val minutes = totalTimeInSeconds / MINUTES_IN_SECONDS % 60
    val seconds = totalTimeInSeconds % 60

    val hourLabel =
        if (hours > 0) "${hours}${context.getString(R.string.hour_abbreviation)} " else ""
    val minutesLabel =
        if (minutes > 0) "${minutes}${context.getString(R.string.minutes_abbreviation)}" else context.getString(
            R.string.zero_minutes
        )
    val secondsLabel =
        if (seconds > 0) "${seconds}${context.getString(R.string.seconds_abbreviation)}" else context.getString(
            R.string.zero_seconds
        )


    return "$hourLabel$minutesLabel $secondsLabel"
}

fun calculateSecondsLeftUntilCountdownIsOver(submitDate: String?, totalTimeInSeconds: Int) : Long {
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

const val DAYS_IN_SECONDS = 86_400
const val HOURS_IN_SECONDS = 3_600
const val MINUTES_IN_SECONDS = 60
const val HOURS_IN_DAY = 24