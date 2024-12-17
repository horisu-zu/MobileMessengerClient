package com.example.testapp.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.templates.Avatar

@Composable
fun ProfileSection(
    userData: UserResponse,
    modifier: Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        val (avatar, userInfo) = createRefs()

        Avatar(
            avatarUrl = userData.avatarUrl,
            modifier = Modifier
                .constrainAs(avatar) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .size(56.dp)
        )

        Column(
            modifier = Modifier
                .constrainAs(userInfo) {
                    start.linkTo(avatar.end, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${userData.firstName} ${userData.lastName}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "@${userData.nickname}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}