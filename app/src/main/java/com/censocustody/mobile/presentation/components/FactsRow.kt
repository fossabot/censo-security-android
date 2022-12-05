package com.censocustody.mobile.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalInfoRow
import com.censocustody.mobile.ui.theme.*

@Composable
fun FactRow(factsData: FactsData, modifier: Modifier = Modifier, hideFinalDivider: Boolean = false) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if(factsData.title.isNotEmpty()) {
            Text(
                text = factsData.title.uppercase(),
                color = StatusGreyText,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                letterSpacing = 0.25.sp
            )
        }

        for ((index, fact) in factsData.facts.withIndex()) {
            ApprovalInfoRow(
                backgroundColor = if (index % 2 == 0) BackgroundLight else BackgroundDark,
                title = fact.first,
                value = fact.second
            )
            if (!hideFinalDivider) {
                Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
            }
        }
    }
}

data class FactsData(
    val title: String = "",
    val facts: List<Pair<String, String>>,
)