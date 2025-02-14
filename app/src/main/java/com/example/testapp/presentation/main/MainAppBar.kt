package com.example.testapp.presentation.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.presentation.templates.inputColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    onDrawerClick: () -> Unit,
    isSearchActive: Boolean,
    searchType: SearchType,
    searchQuery: String,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String, SearchType) -> Unit
) {
    TopAppBar(
        title = {
            if (isSearchActive) {
                SearchTitle(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        onSearchQueryChange(query, searchType)
                    }
                )
            } else {
                Text(text = "Messenger", modifier = Modifier.padding(start = 16.dp))
            }
        },
        navigationIcon = {
            IconButton(onClick = onDrawerClick) {
                Icon(Icons.Default.Menu, contentDescription = "Drawer")
            }
        },
        actions = {
            IconButton(onClick = {
                onSearchActiveChange(!isSearchActive)
                if (!isSearchActive) {
                    onSearchQueryChange(searchQuery, searchType)
                }
            }) {
                Icon(
                    if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "Close Search" else "Search"
                )
            }
        }
    )
}

@Composable
private fun SearchTitle(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp
    )

    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(
            text = "Search...",
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        ) },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                RoundedCornerShape(12.dp)
            ),
        singleLine = true,
        textStyle = textStyle,
        colors = inputColors()
    )
}
