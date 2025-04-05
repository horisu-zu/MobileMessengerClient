package com.example.testapp.presentation.main.maincontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.testapp.presentation.templates.StyledText
import com.example.testapp.utils.Resource
import kotlinx.coroutines.delay

@Composable
fun SummarizeDialog(
    summarizeState: Resource<String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Message Summary",
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    when (summarizeState) {
                        is Resource.Error -> {
                            Text(
                                text = summarizeState.message ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is Resource.Success -> {
                            var visibleText by remember { mutableStateOf("") }
                            val fullText = summarizeState.data ?: "No summary available"

                            LaunchedEffect(fullText) {
                                fullText.forEachIndexed { index, char ->
                                    visibleText = fullText.take(index + 1)
                                    delay(20)
                                }
                            }

                            StyledText(
                                text = visibleText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        else -> {
                            CircularProgressIndicator()
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}