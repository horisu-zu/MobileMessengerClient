package com.example.testapp.presentation.chat.search

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.chat.FilterOption
import com.example.testapp.domain.dto.chat.SortBy

@Composable
fun ChatSearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    filters: List<FilterOption>,
    modifier: Modifier = Modifier,
    sortBy: SortBy? = null,
    onFilterRemove: (FilterOption) -> Unit,
    onSortRemove: () -> Unit
) {
    BasicTextField(
        value = query,
        onValueChange = { newValue ->
            onQueryChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sortBy?.let {
                        InputChip(
                            selected = true,
                            onClick = onSortRemove,
                            label = { Text(text = sortBy.direction.displayValue) }
                        )
                    }
                    filters.forEach { filter ->
                        InputChip(
                            selected = true,
                            onClick = { onFilterRemove(filter) },
                            label = { Text(text = filter.displayValue) }
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            }
        }
    )
}