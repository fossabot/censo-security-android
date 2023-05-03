package com.censocustody.android.screen

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.censocustody.android.common.tag.TestTag
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class ComposeBiometryBlockingUI(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ComposeBiometryBlockingUI>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag(TestTag.biometry_blocking_ui_container) }
    ) {

    val blockingUIColumn: KNode = child {
        hasTestTag(TestTag.biometry_blocking_ui_column)
    }

    val blockingUIText: KNode = child {
        hasTestTag(TestTag.biometry_blocking_ui_text)
    }
}