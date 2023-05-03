package com.censocustody.android.screen

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.censocustody.android.common.tag.TestTag
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class ComposeMainActivity(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ComposeMainActivity>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag(TestTag.main_activity_surface_container) }
    ) {

    val foregroundBlockingUI: KNode = child {
        hasTestTag(TestTag.biometry_blocking_ui_container)
    }

    val centeredTopAppBar: KNode = child {
        hasTestTag(TestTag.centered_top_app_bar)
    }

    val centeredTopAppBarTitle: KNode = child {
        hasTestTag(TestTag.centered_top_app_bar_title)
    }
}