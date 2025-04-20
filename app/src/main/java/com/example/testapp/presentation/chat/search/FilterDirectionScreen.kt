package com.example.testapp.presentation.chat.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.domain.dto.chat.SortBy
import com.example.testapp.domain.dto.chat.SortDirection
import com.example.testapp.domain.dto.chat.SortField

@Composable
fun FilterDirectionScreen(
    sortBy: SortBy?,
    onClick: (SortBy) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        var selectedField = sortBy?.field ?: SortField.Date
        var selectedDirection = sortBy?.direction

        InputChip(
            selected = selectedDirection == SortDirection.Descending,
            label = { Text(text = SortDirection.Descending.displayValue) },
            onClick = {
                onClick(SortBy(field = selectedField, direction = SortDirection.Descending))
            }
        )
        InputChip(
            selected = selectedDirection == SortDirection.Ascending,
            label = { Text(text = SortDirection.Ascending.displayValue) },
            onClick = {
                onClick(SortBy(field = selectedField, direction = SortDirection.Ascending))
            }
        )
    }
}