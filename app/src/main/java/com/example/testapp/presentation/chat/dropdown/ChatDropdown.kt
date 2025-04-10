package com.example.testapp.presentation.chat.dropdown

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import com.example.testapp.presentation.templates.MenuItem

@Composable
fun ChatDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        MenuItem(
            text = stringResource(id = R.string.cdd_info),
            iconRes = R.drawable.ic_info,
            onClick = { onInfoClick() }
        )
        MenuItem(
            text = stringResource(id = R.string.cdd_search),
            iconRes = R.drawable.ic_search,
            onClick = { onSearchClick() }
        )
        MenuItem(
            text = stringResource(id = R.string.cdd_leave),
            iconRes = R.drawable.ic_logout,
            onClick = { onLeaveClick() },
            iconTint = MaterialTheme.colorScheme.error
        )
    }
}