package com.example.testapp.presentation.user.bottomsheet

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.testapp.R
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.user.UserStatus
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.utils.Defaults.calculateUserStatus

@Composable
fun UserCard(
    userData: UserResponse,
    userStatus: UserStatus,
    context: Context
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val (userInfo, divider, userDescription) = createRefs()

        UserInfoItem(
            userData = userData,
            userStatus = userStatus,
            modifier = Modifier.constrainAs(userInfo) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        if(userData.description != null) {
            HorizontalDivider(
                modifier = Modifier.constrainAs(divider) {
                    top.linkTo(userInfo.bottom, margin = 4.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
            }, color = MaterialTheme.colorScheme.background)

            DescriptionItem(
                description = userData.description,
                modifier = Modifier.constrainAs(userDescription) {
                    top.linkTo(divider.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                context = context
            )
        }
    }
}

@Composable
fun UserInfoItem(
    userData: UserResponse,
    userStatus: UserStatus,
    modifier: Modifier
) {
    ConstraintLayout(modifier = modifier) {
        val (avatar, nameColumn, status) = createRefs()

        Avatar(
            avatarUrl = userData.avatarUrl,
            modifier = Modifier
                .size(48.dp)
                .constrainAs(avatar) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            userStatus = userStatus.onlineStatus
        )

        Column(
            modifier = Modifier
                .constrainAs(nameColumn) {
                    start.linkTo(avatar.end, margin = 16.dp)
                    top.linkTo(parent.top)
                }
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${userData.firstName} ${userData.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "@${userData.nickname}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }

        if (!userStatus.onlineStatus) {
            Text(
                text = calculateUserStatus(LocalContext.current, userStatus),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.constrainAs(status) {
                    start.linkTo(nameColumn.start)
                    top.linkTo(nameColumn.bottom, margin = 4.dp)
                }
            )
        }
    }
}

@Composable
fun DescriptionItem(
    description: String,
    modifier: Modifier = Modifier,
    context: Context
) {
    ConstraintLayout(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val (descriptionLabel, descriptionText) = createRefs()

        Text(
            text = context.getString(R.string.cbs_description),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            modifier = Modifier.constrainAs(descriptionLabel) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                width = Dimension.preferredWrapContent
            }
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.constrainAs(descriptionText) {
                top.linkTo(descriptionLabel.bottom, margin = 2.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
            maxLines = 5
        )
    }
}