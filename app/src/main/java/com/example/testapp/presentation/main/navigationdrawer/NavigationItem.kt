package com.example.testapp.presentation.main.navigationdrawer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.navigation.NavigationItemData

@Composable
fun NavigationItem(
    item: NavigationItemData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(text = item.title, fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface) },
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                painter = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        badge = {
            item.badgeCount?.let {
                Text(text = it.toString(), fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color.Transparent,
            unselectedContainerColor = Color.Transparent
        )
    )
}