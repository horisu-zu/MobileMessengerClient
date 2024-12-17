package com.example.testapp.presentation.main.group

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepAppBar(
    currentStep: Int,
    totalSteps: Int,
    onBackPressed: () -> Unit,
    onNextPressed: () -> Unit,
    isBackEnabled: Boolean = true,
    isNextEnabled: Boolean = true
) {
    TopAppBar(
        title = {
            StepIndicator(
                stepsCount = totalSteps,
                currentStep = currentStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackPressed,
                enabled = isBackEnabled
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isBackEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.outline
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNextPressed,
                enabled = isNextEnabled
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = if (isNextEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.outline
                )
            }
        }
    )
}