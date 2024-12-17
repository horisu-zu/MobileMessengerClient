package com.example.testapp.presentation.main.searchscreen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var inviteCode by remember { mutableStateOf(List(8) { "" }) }
    val focusRequesters = remember { List(8) { FocusRequester() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Code") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Enter 8-Character Invite Code")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    inviteCode.forEachIndexed { index, char ->
                        InviteCodeCharInput(
                            value = char,
                            onValueChange = { newChar ->
                                val newInviteCode = inviteCode.toMutableList()
                                if (newChar.isEmpty() && index > 0) {
                                    newInviteCode[index] = ""
                                    inviteCode = newInviteCode
                                    focusRequesters[index - 1].requestFocus()
                                } else if (newChar.length <= 1) {
                                    newInviteCode[index] = newChar.uppercase()
                                    inviteCode = newInviteCode
                                    if (newChar.isNotEmpty() && index < 7) {
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                }
                            },
                            focusRequester = focusRequesters[index],
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(inviteCode.joinToString("")) },
                enabled = inviteCode.all { it.isNotEmpty() && it[0].isLetterOrDigit() }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InviteCodeCharInput(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.small)
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                innerTextField()
            }
        }
    )
}