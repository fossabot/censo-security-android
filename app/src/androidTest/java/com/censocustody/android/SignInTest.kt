package com.censocustody.android

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.censocustody.android.screen.ComposeSignInScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.components.composesupport.interceptors.behavior.impl.systemsafety.SystemDialogSafetySemanticsBehaviorInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.params.FlakySafetyParams
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInTest : TestCase(
    kaspressoBuilder = Kaspresso.Builder.withComposeSupport(
        customize = {
            flakySafetyParams = FlakySafetyParams.custom(timeoutMs = 5000, intervalMs = 1000)
        },
        lateComposeCustomize = { composeBuilder ->
            composeBuilder.semanticsBehaviorInterceptors = composeBuilder.semanticsBehaviorInterceptors.filter {
                it !is SystemDialogSafetySemanticsBehaviorInterceptor
            }.toMutableList()
        }
    )
) {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun test() = run {
        step("Assert logged out user is on Sign In Screen") {
            ComposeScreen.onComposeScreen<ComposeSignInScreen>(composeTestRule) {
                simpleSignInColumn {
                    assertIsDisplayed()
                }
            }
        }

        step("Attempt sign in") {
            ComposeScreen.onComposeScreen<ComposeSignInScreen>(composeTestRule) {
                simpleSignInButton {
                    assertIsDisplayed()
                    assertTextContains("Sign In")
                }
            }
        }
    }

}