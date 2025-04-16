package com.example.testapp.presentation.chat.dropdown

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R

@Composable
fun ChatDropdown(
    isGroupChat: Boolean,
    isAdmin: Boolean,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onAdminClick: () -> Unit,
    onInfoClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.cdd_search)) },
            leadingIcon = { Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            ) },
            onClick = { onSearchClick() }
        )
        if(isGroupChat) {
            if(isAdmin) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.cdd_admin)) },
                    leadingIcon = { Icon(
                        painter = painterResource(R.drawable.ic_admin),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    ) },
                    onClick = { onAdminClick() }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.cdd_info)) },
                leadingIcon = { Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                ) },
                onClick = { onInfoClick() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.cdd_leave), color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                ) },
                onClick = { onLeaveClick() }
            )
        }
    }
}