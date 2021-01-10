package me.zeroeightsix.kami.util.text

interface Detector {
    infix fun detect(input: CharSequence): Boolean

    infix fun detectNot(input: CharSequence) = !detect(input)
}

interface RemovableDetector {
    fun removedOrNull(input: CharSequence): CharSequence?
}

interface PlayerDetector {
    fun playerName(input: CharSequence): String?
}

interface PrefixDetector : Detector, RemovableDetector {
    val prefix: CharSequence

    override fun detect(input: CharSequence) = input.startsWith(prefix)

    override fun removedOrNull(input: CharSequence) = if (detect(input)) input.removePrefix(prefix) else null
}

interface RegexDetector : Detector, RemovableDetector {
    val regexes: Array<out Regex>

    override infix fun detect(input: CharSequence) = regexes.any { it.containsMatchIn(input) }

    fun matchedRegex(input: CharSequence) = regexes.find { it.containsMatchIn(input) }

    override fun removedOrNull(input: CharSequence): CharSequence? = matchedRegex(input)?.let { regex ->
        input.replace(regex, "").takeIf { it.isNotBlank() }
    }
}
