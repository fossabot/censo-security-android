package com.strikeprotocols.mobile.common

fun convertSecondsIntoCountdownText(totalTimeInSeconds: Int): String {
    if(totalTimeInSeconds <= 0) return ""

    val days = totalTimeInSeconds / DAYS_IN_SECONDS
    val hours = totalTimeInSeconds / HOURS_IN_SECONDS % HOURS_IN_DAY
    val minutes = totalTimeInSeconds / MINUTES_IN_SECONDS % 60
    val seconds = totalTimeInSeconds % 60

    return "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
}

const val DAYS_IN_SECONDS = 86_400
const val HOURS_IN_SECONDS = 3_600
const val MINUTES_IN_SECONDS = 60
const val HOURS_IN_DAY = 24