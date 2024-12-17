package com.example.testapp.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.R

@Composable
fun ProfileDropdown(
    isExpanded: Boolean,
    onEditClick: () -> Unit,
    onChangeAvatarClick: () -> Unit,
    onChangeColorClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { onDismissRequest() }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.profile_menu_edit_info)) },
            onClick = { onEditClick() },
            leadingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_menu_edit_info)
                )
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.profile_menu_change_avatar)) },
            onClick = {
                onChangeAvatarClick()
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Face,
                    contentDescription = stringResource(R.string.profile_menu_change_avatar)
                )
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.profile_menu_change_color)) },
            onClick = { onChangeColorClick() },
            leadingIcon = {
                Icon(
                    Icons.Default.AccountBox,
                    contentDescription = stringResource(R.string.profile_menu_change_color)
                )
            }
        )
    }
}