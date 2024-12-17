package com.example.testapp.presentation.main.navigationdrawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.presentation.templates.ThemeIcon

@Composable
fun UserInfoHeader(
    currentUser: UserResponse?,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)? = null,
    onThemeToggle: () -> Unit,
    isDarkTheme: Boolean,
) {
    val userName = "${currentUser?.firstName} ${currentUser?.lastName}".trim()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Avatar(
                avatarUrl = currentUser?.avatarUrl ?: "Empty URL",
                onClick = onAvatarClick
            )
            ThemeIcon(
                onThemeToggle = onThemeToggle,
                isDarkTheme = isDarkTheme
            )
        }
        Text(
            text = userName.takeIf { it.isNotBlank() } ?: "No name",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}