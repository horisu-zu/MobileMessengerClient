package com.example.testapp.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

object MarkdownString {
    private val boldRegex = """(?<=\*\*)(.*?)(?=\*\*)""".toRegex()
    private val italicRegex = """\*(.*?)\*""".toRegex()
    private val strikethroughRegex = """~~(.*?)~~""".toRegex()
    private val linkRegex = """\[(.*?)]\((.*?)\)""".toRegex()

    enum class TokenType {
        BOLD,
        ITALIC,
        STRIKETHROUGH,
        LINK
    }

    data class MarkdownToken(
        val type: TokenType,
        val start: Int,
        val end: Int,
        val groups: List<String>
    )

    fun parseMarkdown(text: String, color: Color): AnnotatedString {
        val tokens = mutableListOf<MarkdownToken>()
        fun addMatches(pattern: Regex, type: TokenType, groupCount: Int) {
            pattern.findAll(text).forEach { result ->
                val matchedGroups = (1..groupCount).map { i -> result.groups[i]?.value ?: "" }
                tokens += MarkdownToken(
                    type = type,
                    start = result.range.first,
                    end = result.range.last + 1,
                    groups = matchedGroups
                )
            }
        }

        addMatches(boldRegex, TokenType.BOLD, 1)
        addMatches(italicRegex, TokenType.ITALIC, 1)
        addMatches(strikethroughRegex, TokenType.STRIKETHROUGH, 1)
        addMatches(linkRegex, TokenType.LINK, 2)

        tokens.sortBy { it.start }

        val builder = AnnotatedString.Builder()
        var currentIndex = 0

        fun appendGapText(upTo: Int) {
            if (currentIndex < upTo) {
                builder.append(text.substring(currentIndex, upTo))
                currentIndex = upTo
            }
        }

        tokens.forEach { token ->
            appendGapText(token.start)

            when(token.type) {
                TokenType.BOLD -> {
                    val content = token.groups[0]
                    builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(content)
                    }
                }
                TokenType.ITALIC -> {
                    val content = token.groups[0]
                    builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(content)
                    }
                }
                TokenType.STRIKETHROUGH -> {
                    val content = token.groups[0]
                    builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(content)
                    }
                }
                TokenType.LINK -> {
                    val (linkText, url) = token.groups
                    builder.pushStringAnnotation(
                        tag = "URL",
                        annotation = url
                    )
                    builder.withStyle(SpanStyle(
                        color = color,
                        textDecoration = TextDecoration.Underline)
                    ) {
                        append(linkText)
                    }
                    builder.pop()
                }
            }

            currentIndex = token.end
        }

        appendGapText(text.length)

        return builder.toAnnotatedString()
    }
}