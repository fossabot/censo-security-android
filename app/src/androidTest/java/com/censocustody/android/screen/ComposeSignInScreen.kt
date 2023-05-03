package com.censocustody.android.screen

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.censocustody.android.common.tag.TestTag
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class ComposeSignInScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ComposeSignInScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag(TestTag.sign_in_screen_container) }
    ) {

    val simpleSignInColumn: KNode = child {
        hasTestTag(TestTag.sign_in_screen_content_column)
    }

    val simpleSignInButton: KNode = child {
        hasTestTag(TestTag.sign_in_screen_sign_in_button)
    }
}