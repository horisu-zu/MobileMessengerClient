package com.example.testapp.presentation.chat.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.testapp.R
import com.example.testapp.domain.dto.chat.SearchFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSearchAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filters: List<SearchFilter>,
    onBackClick: () -> Unit,
    onFilterIconClick: () -> Unit,
    onFilterRemove: (SearchFilter) -> Unit
) {
    TopAppBar(
        title = {
            ChatSearchTextField(
                query = searchQuery,
                onQueryChange = { query -> onSearchQueryChange(query) },
                filters = filters,
                onFilterRemove = { filterItem -> onFilterRemove(filterItem) }
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onFilterIconClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "Filter",
                )
            }
        }
    )
}