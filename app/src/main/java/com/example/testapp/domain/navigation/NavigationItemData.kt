package com.example.testapp.domain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.testapp.R

data class NavigationItemData(
    val title: String,
    val icon: Painter,
    val badgeCount: Int? = null
)

@Composable
fun navigationItemList(): List<NavigationItemData> {
    return listOf(
        NavigationItemData(
            title = stringResource(id = R.string.nav_main),
            icon = painterResource(id = R.drawable.ic_home)
        ),
        NavigationItemData(
            title = stringResource(id = R.string.nav_settings),
            icon = painterResource(id = R.drawable.ic_settings)
        ),
        /*NavigationItemData(
            title = stringResource(id = R.string.nav_rules),
            icon = painterResource(id = R.drawable.ic_info)
        ),
        NavigationItemData(
            title = stringResource(id = R.string.nav_new),
            icon = painterResource(id = R.drawable.ic_update)
        )*/
    )
}
