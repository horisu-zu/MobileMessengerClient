package com.example.testapp.presentation.user.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.testapp.R
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.ChatParticipant
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.utils.ImageSource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MemberInfo(
    userData: UserResponse,
    chatParticipant: ChatParticipant,
    avatarUrl: String,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val (memberLabel, createdAtItem, dot, joinedAtItem) = createRefs()

        Text(
            text = stringResource(id = R.string.ubs_members),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(memberLabel) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        )

        UserInfoItem(
            imageSource = ImageSource.Drawable(R.drawable.ic_logo),
            title = "Created at",
            timestamp = userData.createdAt,
            modifier = Modifier.constrainAs(createdAtItem) {
                top.linkTo(memberLabel.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            }
        )

        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.constrainAs(dot) {
                top.linkTo(createdAtItem.top)
                bottom.linkTo(createdAtItem.bottom)
                start.linkTo(createdAtItem.end, margin = 8.dp)
            }
        )

        UserInfoItem(
            imageSource = ImageSource.Url(avatarUrl),
            title = "Joined at",
            timestamp = chatParticipant.joinedAt,
            modifier = Modifier.constrainAs(joinedAtItem) {
                top.linkTo(createdAtItem.top)
                bottom.linkTo(createdAtItem.bottom)
                start.linkTo(dot.end, margin = 8.dp)
            }
        )
    }
}

@Composable
private fun UserInfoItem(
    imageSource: ImageSource,
    title: String,
    timestamp: Instant,
    modifier: Modifier = Modifier
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.getDefault())
    }
    val formattedDate = remember(timestamp) {
        timestamp.atZone(ZoneId.systemDefault()).format(formatter)
    }

    ConstraintLayout(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        val (image, date) = createRefs()

        val imageModifier = Modifier
            .constrainAs(image) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
            .size(24.dp)

        when (imageSource) {
            is ImageSource.Drawable -> {
                Box(
                    modifier = imageModifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = imageSource.resId),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is ImageSource.Url -> {
                Avatar(
                    avatarUrl = imageSource.url,
                    modifier = imageModifier,
                    isGroupChat = true
                )
            }
        }

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.constrainAs(date) {
                start.linkTo(image.end, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )
    }
}