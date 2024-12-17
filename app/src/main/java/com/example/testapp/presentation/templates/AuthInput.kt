package com.example.testapp.presentation.templates

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.testapp.R

@Composable
fun AuthInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    iconSize: Dp = 24.dp,
    passwordVisibility: Boolean? = null,
    onPasswordVisibilityChange: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (isError) MaterialTheme.colorScheme.error
        else if (isFocused) MaterialTheme.colorScheme.outline
        else MaterialTheme.colorScheme.background, label = ""
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp)
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text(text = label, color = MaterialTheme.colorScheme.outline) },
            leadingIcon = leadingIcon?.let { { IconBox(iconSize, it) } },
            trailingIcon = trailingIcon?.let { { IconBox(iconSize, it) } }
                ?: passwordVisibility?.let { visibility ->
                    { IconBox(iconSize) {
                        IconButton(onClick = onPasswordVisibilityChange!!) {
                            Image(
                                painter = if (visibility)
                                    painterResource(id = R.drawable.visible)
                                else
                                    painterResource(id = R.drawable.hidden),
                                modifier = Modifier.size(iconSize),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                contentDescription = if (visibility) "Hide password" else "Show password"
                            )
                        }
                    }}
                },
            isError = isError,
            visualTransformation = if (keyboardType == KeyboardType.Password &&
                passwordVisibility == false) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = inputColors(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@Composable
private fun IconBox(size: Dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun inputColors() = TextFieldDefaults.textFieldColors(
    containerColor = MaterialTheme.colorScheme.onPrimary,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary
)
