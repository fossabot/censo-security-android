package com.strikeprotocols.mobile.common

import androidx.navigation.NavOptionsBuilder
import com.strikeprotocols.mobile.presentation.Screen

fun NavOptionsBuilder.popUpToTop(shouldPopTopDestination: Boolean = true) {
    popUpTo(Screen.START_DESTINATION_ID) {
        inclusive = shouldPopTopDestination
    }
}