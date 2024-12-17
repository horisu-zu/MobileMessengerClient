package com.example.testapp.presentation.main.navigationdrawer

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.navigation.NavigationItemData
import com.example.testapp.presentation.viewmodel.ThemeViewModel
import com.example.testapp.presentation.viewmodel.user.AuthManager
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainNavigationDrawer(
    currentUser: UserResponse?,
    parentNavController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    navigationData: List<NavigationItemData>,
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    authManager: AuthManager
) {
    val switchState by themeViewModel.isDarkThemeEnabled.collectAsState()
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.65f),
        drawerContainerColor = MaterialTheme.colorScheme.background,
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        UserInfoHeader(
            currentUser = currentUser,
            onAvatarClick = {
                scope.launch {
                    parentNavController.navigate("profile")
                    drawerState.close()
                }
            },
            onThemeToggle = {
                scope.launch {
                    Log.d("Theme Switch", "Switch theme from $switchState")
                    val newTheme = !switchState
                    themeViewModel.setTheme(newTheme)
                }
            },
            isDarkTheme = switchState
        )

        NavigationItemsCard(navigationData, selectedItemIndex) { index ->
            selectedItemIndex = index
            scope.launch { drawerState.close() }
        }

        LogoutItem {
            scope.launch {
                drawerState.close()
                authManager.logout {
                    parentNavController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }
        }
    }
}