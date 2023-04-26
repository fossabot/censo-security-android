package com.censocustody.android.common

import androidx.navigation.NavOptionsBuilder
import com.censocustody.android.presentation.Screen

fun NavOptionsBuilder.popUpToTop(shouldPopTopDestination: Boolean = true) {
    popUpTo(Screen.START_DESTINATION_ID) {
        inclusive = shouldPopTopDestination
    }
}