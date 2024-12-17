package com.example.testapp.presentation.main.searchscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddFab(
    mainNavController: NavController,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if(expanded) 45f else 0f, label = ""
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { mainNavController.navigate("groupAddScreen") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Group Chat Add") },
                    text = { Text("Add Group Chat") }
                )
                ExtendedFloatingActionButton(
                    onClick = { /**/ },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    icon = { Icon(Icons.Filled.Edit, contentDescription = "Channel Add") },
                    text = { Text("Add Channel") }
                )
            }
        }
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Expand",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}