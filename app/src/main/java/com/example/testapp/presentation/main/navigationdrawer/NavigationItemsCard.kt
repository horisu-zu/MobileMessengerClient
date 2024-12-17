package com.example.testapp.presentation.main.navigationdrawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.navigation.NavigationItemData

@Composable
fun NavigationItemsCard(
    data: List<NavigationItemData>,
    selectedItemIndex: Int,
    onItemClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column {
            data.forEachIndexed { index, item ->
                NavigationItem(item, index == selectedItemIndex) {
                    onItemClick(index)
                }
            }
        }
    }
}