package com.example.testapp.presentation.chat.admin

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.R
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.presentation.chat.main.RestrictionBottomSheet
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.presentation.viewmodel.chat.ChatRestrictionViewModel
import com.example.testapp.utils.Converter
import java.time.Instant

@Composable
fun ChatRestrictionsPage(
    expireType: RestrictionExpireType,
    usersList: List<UserResponse>,
    onClearRestriction: (String) -> Unit,
    onUpdateRestriction: (ChatRestriction) -> Unit,
    chatRestrictionViewModel: ChatRestrictionViewModel = hiltViewModel()
) {
    val restrictionsState by chatRestrictionViewModel.restrictionState.collectAsState()
    val isExpired = expireType == RestrictionExpireType.EXPIRED
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
    ) {
        items(restrictionsState[expireType] ?: emptyList()) { restriction ->
            ChatRestrictionItem(
                restriction = restriction,
                userData = usersList.first { it.userId == restriction.userId },
                applierData = usersList.first { it.userId == restriction.createdBy },
                isExpired = isExpired,
                context = context,
                onUpdateRestriction = onUpdateRestriction,
                onClearRestriction = onClearRestriction
            )
        }
    }
}

@Composable
private fun ChatRestrictionItem(
    restriction: ChatRestriction,
    userData: UserResponse,
    applierData: UserResponse,
    isExpired: Boolean,
    context: Context,
    onUpdateRestriction: (ChatRestriction) -> Unit,
    onClearRestriction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Arrow rotation"
    )

    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                avatarUrl = userData.avatarUrl,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "@${userData.nickname}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = restriction.type
            )
            IconButton(
                onClick = { isExpanded = !isExpanded }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Show less" else "Show more",
                    modifier = Modifier.rotate(rotationState)
                )
            }
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top)
            ) {
                if(!restriction.reason.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${stringResource(id = R.string.reason_title)}: ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = restriction.reason,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        RestrictionDateItem(
                            iconVector = Icons.Default.Add,
                            date = restriction.createdAt,
                            context = context
                        )
                        RestrictionDateItem(
                            iconVector = Icons.Default.Done,
                            context = context,
                            date = restriction.expiresAt
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            avatarUrl = applierData.avatarUrl,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "@${applierData.nickname}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
                if(!isExpired) {
                    Row(
                        Modifier.fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxHeight().clip(RoundedCornerShape(8.dp))
                                .clickable { onClearRestriction(restriction.restrictionId) }
                                .background(MaterialTheme.colorScheme.background)
                                .padding(4.dp)
                        )
                        Row(
                            Modifier.fillMaxHeight().clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .clickable { onUpdateRestriction(restriction) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_update),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.crp_update),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestrictionDateItem(
    iconVector: ImageVector,
    date: Instant?,
    context: Context
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = Converter.formatInstant(date, context = context, includeTime = true),
            style = MaterialTheme.typography.labelMedium
        )
    }
}