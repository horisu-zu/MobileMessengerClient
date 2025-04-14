package com.example.testapp.presentation.chat.main

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.R
import com.example.testapp.domain.dto.chat.ChatRestrictionRequest
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.domain.models.chat.RestrictionType
import com.example.testapp.presentation.templates.BasicTextInput
import com.example.testapp.presentation.templates.section.SectionItem
import com.example.testapp.presentation.user.bottomsheet.UserInfoItem
import com.example.testapp.presentation.viewmodel.chat.ChatRestrictionInputViewModel
import com.example.testapp.utils.Converter.formatDuration
import com.example.testapp.utils.Resource
import java.time.Duration
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestrictionBottomSheet(
    chatId: String,
    currentUserId: String?,
    userData: UserResponse,
    onDismiss: () -> Unit,
    restrictionInputViewModel: ChatRestrictionInputViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val inputState by restrictionInputViewModel.inputState.collectAsState()
    val restrictionState by restrictionInputViewModel.restrictionState.collectAsState()

    LaunchedEffect(Unit) {
        restrictionInputViewModel.updateInputState(
            ChatRestrictionRequest(
                userId = userData.userId,
                type = RestrictionType.MUTE,
                duration = Duration.ZERO.toString(),
                reason = "",
                createdBy = currentUserId ?: ""
            )
        )
    }

    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = null,
        onDismissRequest = onDismiss
    ) {
        Column (
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                UserInfoItem(
                    userData = userData,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            RestrictionDescSection(
                listOf(
                    SectionItem.Input(
                        label = "Reason",
                        value = inputState.reason ?: "",
                        onValueChange = { newValue ->
                            restrictionInputViewModel.updateInputState(inputState.copy(reason = newValue))
                        }
                    ),/**/
                )
            )
            RestrictionSlider(
                duration = Duration.parse(inputState.duration),
                context = context,
                onDurationChange = { newValue ->
                    restrictionInputViewModel.updateInputState(inputState.copy(duration = newValue.toString()))
                }
            )
            ChangeRow(
                restrictionState = restrictionState,
                onSave = {
                    restrictionInputViewModel.createUserRestriction(chatId, inputState)
                        .invokeOnCompletion { throwable ->
                            when(throwable) {
                                null -> onDismiss()
                                else -> Toast.makeText(context, "Error: ${throwable.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                onClearState = {
                    restrictionInputViewModel.clearInputState()
                }
            )
        }
    }
}

@Composable
private fun RestrictionSlider(
    duration: Duration,
    onDurationChange: (Duration) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val durationSteps = listOf(
        Duration.ZERO,
        Duration.ofMinutes(1),
        Duration.ofMinutes(15),
        Duration.ofHours(1),
        Duration.ofHours(3),
        Duration.ofHours(6),
        Duration.ofHours(12),
        Duration.ofDays(1),
        Duration.ofDays(3),
        Duration.ofDays(5),
        Duration.ofDays(7)
    )

    val durationIndex = durationSteps.indexOfLast { it <= duration }.coerceIn(0, durationSteps.lastIndex)
    val sliderValue = durationIndex.toFloat()

    Column (
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "${stringResource(R.string.duration_title)}: ${context.formatDuration(duration)}",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                val index = newValue.roundToInt().coerceIn(0, durationSteps.lastIndex)
                onDurationChange(durationSteps[index])
            },
            steps = durationSteps.size - 1,
            valueRange = 0f..(durationSteps.size  - 1).toFloat(),
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun RestrictionDescSection(
    sectionItems: List<SectionItem.Input>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        sectionItems.forEach { item ->
            RestrictionDescInputItem(
                title = item.label,
                value = item.value,
                onValueChange = item.onValueChange
            )
        }
    }
}

@Composable
private fun RestrictionDescInputItem(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            )
        )
        BasicTextInput(
            value = value,
            onValueChange = onValueChange,
            minLines = 3
        )
    }
}

@Composable
private fun ChangeRow(
    onSave: () -> Unit,
    onClearState: () -> Unit,
    restrictionState: Resource<ChatRestriction>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
    ) {
        IconButton(
            onClick = onClearState
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (restrictionState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(
                    onClick = onSave
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}