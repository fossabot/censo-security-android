package com.strikeprotocols.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.ui.theme.*

@Composable
fun FactRow(factsData: FactsData, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = DetailInfoDarkBackground)
                .padding(top = 2.dp, bottom = 2.dp)
        ) {
            Text(
                text = factsData.title,
                textAlign = TextAlign.Center,
                color = AccountTextGrey
            )
        }
        for (fact in factsData.facts) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = DetailInfoLightBackground)
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = fact.first, color = GreyText)
                if (fact.second.isNotEmpty()) {
                    Text(text = fact.second, color = AccountTextGrey)
                }
            }
        }
    }
}

data class FactsData(
    val title: String,
    val facts: List<Pair<String, String>>,
)

@Preview(showBackground = true)
@Composable
fun PREEEEVIEW() {
    FactRow(
        FactsData(
            title = "Title",
            facts = listOf(Pair("Label", "Content"))
        )
    )
}