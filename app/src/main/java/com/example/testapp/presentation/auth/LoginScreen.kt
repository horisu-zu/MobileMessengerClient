package com.example.testapp.presentation.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.presentation.templates.AuthInput
import com.example.testapp.presentation.templates.LogoIcon
import com.example.testapp.presentation.templates.OperationButton
import com.example.testapp.presentation.templates.ThemeIcon
import com.example.testapp.presentation.viewmodel.ThemeViewModel
import com.example.testapp.presentation.viewmodel.user.AuthState
import com.example.testapp.presentation.viewmodel.user.AuthViewModel
import com.example.testapp.utils.DataStoreUtil
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    parentNavController: NavController,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    dataStoreUtil: DataStoreUtil
) {
    val authState by authViewModel.authState.collectAsState()
    val switchState by themeViewModel.isDarkThemeEnabled.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val isEnabled by remember(email, password) {
        derivedStateOf { email.isNotBlank() && password.isNotBlank() }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LogoIcon()
                    Text(
                        text = stringResource(id = R.string.auth_login_label),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    AuthInput(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(id = R.string.auth_email),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") }
                    )
                    AuthInput(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(id = R.string.auth_password),
                        keyboardType = KeyboardType.Password,
                        passwordVisibility = passwordVisibility,
                        onPasswordVisibilityChange = { passwordVisibility = !passwordVisibility},
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") }
                    )

                    OperationButton(
                        onClick = {
                            authViewModel.signIn(email, password)
                        },
                        label = stringResource(id = R.string.auth_login_label),
                        enabled = isEnabled
                    )
                }
            }
            TextButton(
                onClick = {
                    navController.navigate("signup")
                    //showToast = false
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(id = R.string.auth_sign))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 24.dp)
        ) {
            ThemeIcon(
                onThemeToggle = {
                    coroutineScope.launch {
                        val newTheme = !switchState
                        themeViewModel.setTheme(newTheme)
                    }
                },
                isDarkTheme = switchState
            )
        }
    }

    when (authState) {
        is AuthState.Loading -> { Log.d("LoginScreen", "Loading...") }
        is AuthState.Authenticated -> {
            LaunchedEffect(authState) {
                Log.d("LoginScreen", "Navigating to main screen")
                parentNavController.navigate("main")
            }
        }
        is AuthState.Error -> {
            /*LaunchedEffect(authState) {
                Log.d("LoginScreen", "Showing error toast: " +
                        (authState as AuthState.Error).message)
                showToast = true
                toastMessage = (authState as AuthState.Error).message
            }*/
        }
        is AuthState.Unauthenticated -> {}
    }
}