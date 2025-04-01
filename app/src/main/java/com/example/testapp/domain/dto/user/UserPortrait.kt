package com.example.testapp.domain.dto.user

data class UserPortrait(
    val characteristics: List<UserCharacteristic>,
    val portraitSummary: String
) {
    companion object {
        fun parseUserPortrait(responseText: String): UserPortrait {
            val lines = responseText.trim().split("\n")
            val characteristicRegex = """(.+?)\s+(.*?)\s+—\s+(\d+)%""".toRegex()

            val characteristics = mutableListOf<UserCharacteristic>()
            var summaryStartIndex = -1

            for ((index, line) in lines.withIndex()) {
                val match = characteristicRegex.find(line)
                if (match != null) {
                    val (emojiPart, name, percentageStr) = match.destructured
                    characteristics.add(
                        UserCharacteristic(
                            emoji = emojiPart.trim(),
                            name = name.trim(),
                            percentage = percentageStr.toInt()
                        )
                    )
                } else if (line.isNotBlank() && characteristics.isNotEmpty()) {
                    summaryStartIndex = index
                    break
                }
            }

            val summary = if (summaryStartIndex >= 0) {
                lines.subList(summaryStartIndex, lines.size).joinToString("\n")
            } else {
                ""
            }

            return UserPortrait(characteristics, summary)
        }
    }
}

data class UserCharacteristic(
    val emoji: String,
    val name: String,
    val percentage: Int
)