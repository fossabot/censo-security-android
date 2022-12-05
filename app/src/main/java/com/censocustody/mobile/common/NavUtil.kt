package com.censocustody.mobile.common

import androidx.navigation.NavOptionsBuilder
import com.censocustody.mobile.presentation.Screen

fun NavOptionsBuilder.popUpToTop(shouldPopTopDestination: Boolean = true) {
    popUpTo(Screen.START_DESTINATION_ID) {
        inclusive = shouldPopTopDestination
    }
}