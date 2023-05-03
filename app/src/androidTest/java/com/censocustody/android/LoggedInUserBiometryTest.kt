package com.censocustody.android

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.censocustody.android.screen.ComposeMainActivity
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.components.composesupport.interceptors.behavior.impl.systemsafety.SystemDialogSafetySemanticsBehaviorInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.params.FlakySafetyParams
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LoggedInUserBiometryTest : TestCase(
    kaspressoBuilder = Kaspresso.Builder.withComposeSupport(
        customize = {
            flakySafetyParams = FlakySafetyParams.custom(timeoutMs = 5000, intervalMs = 1000)
        },
        lateComposeCustomize = { composeBuilder ->
            composeBuilder.semanticsBehaviorInterceptors =
                composeBuilder.semanticsBehaviorInterceptors.filter {
                    it !is SystemDialogSafetySemanticsBehaviorInterceptor
                }.toMutableList()
        }
    )
) {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun test() = run {
        step("Assert fully authenticated logged in user is prompted for biometry on launch") {
            ComposeScreen.onComposeScreen<ComposeMainActivity>(composeTestRule) {
                foregroundBlockingUI {
                    assertIsDisplayed()
                    assertIsNotFocused()
                }

                adbServer.performAdb("-e emu finger touch 1")
            }
        }

        step("Assert biometric success takes user to Approvals screen") {
            ComposeScreen.onComposeScreen<ComposeMainActivity>(composeTestRule) {
                centeredTopAppBar {
                    assertIsDisplayed()
                }

                centeredTopAppBarTitle {
                    assertIsDisplayed()
                    assertTextContains("Approvals")
                }
            }
        }
    }
}