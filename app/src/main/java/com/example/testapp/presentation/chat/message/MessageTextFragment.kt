package com.example.testapp.presentation.chat.message

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import com.example.testapp.utils.MarkdownString
import androidx.core.net.toUri

@Composable
fun MessageTextFragment(
    message: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val annotatedString = MarkdownString.parseMarkdown(message, color = MaterialTheme.colorScheme.primary)
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    SelectionContainer(modifier = modifier) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures { offsetPos: Offset ->  
                    textLayoutResult?.let { layoutResult ->
                        val offset = layoutResult.getOffsetForPosition(offsetPos)
                        annotatedString
                            .getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.let { annotation ->
                                val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                                context.startActivity(intent)
                            }
                    }
                }
            },
            onTextLayout = { textLayoutResult = it }
        )
    }
}