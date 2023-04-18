package com.censocustody.android.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.ApprovalInfoRow
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.UserImageRow
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.UserInfoRow
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.UserRoleRow
import com.censocustody.android.ui.theme.*

@Composable
fun FactRow(factsData: FactsData, modifier: Modifier = Modifier, hideFinalDivider: Boolean = false) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if(factsData.title.isNotEmpty()) {
            Text(
                text = factsData.title.uppercase(),
                color = DarkGreyText,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                letterSpacing = 0.25.sp
            )
        }

        for ((index, fact) in factsData.facts.withIndex()) {
            Divider(modifier = Modifier.height(1.0.dp), color = BorderGrey)

            val rowBackgroundColor = if (index % 2 == 0) BackgroundDark else BackgroundLight
            when (fact) {
                is RowData.KeyValueRow ->
                    ApprovalInfoRow(
                        backgroundColor = rowBackgroundColor,
                        title = fact.key,
                        value = fact.value
                    )
                is RowData.UserInfo ->
                    UserInfoRow(
                        backgroundColor = rowBackgroundColor,
                        name = fact.name,
                        email = fact.email,
                        image = fact.image
                    )
                is RowData.UserImage ->
                    UserImageRow(
                        backgroundColor = rowBackgroundColor,
                        name = fact.name,
                        image = fact.image
                    )
                is RowData.UserRole ->
                    UserRoleRow(
                        backgroundColor = rowBackgroundColor,
                        name = fact.name,
                        email = fact.email,
                        role = fact.role,
                        image = fact.image
                    )
            }
            if (!hideFinalDivider) {
                Divider(modifier = Modifier.height(1.0.dp), color = BorderGrey)
            }
        }
    }
}

data class FactsData(
    val title: String = "",
    val facts: List<RowData>,
)

sealed class RowData {
    data class KeyValueRow(val key: String, val value: String): RowData()
    data class UserInfo(val name: String, val email: String, val image: String? = null): RowData()
    data class UserImage(val name: String, val image: String?): RowData()
    data class UserRole(val name: String, val email: String, val role: String, val image: String? = null): RowData()
}
