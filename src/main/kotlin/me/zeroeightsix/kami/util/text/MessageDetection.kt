package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.feature.module.misc.ChatEncryption
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.util.Wrapper

object MessageDetection {
    enum class Command : PrefixDetector {
        KAMI {
            override val prefix: CharSequence
                get() = Settings.commandPrefix.toString()
        },
        BARITONE {
            override val prefix: CharSequence
                get() = BaritoneIntegration.prefix ?: ""
        }
    }

    enum class Message : Detector, PlayerDetector, RemovableDetector {
        SELF {
            override fun detect(input: CharSequence) = Wrapper.getPlayer()?.name?.let {
                input.startsWith("<${it}>")
            } ?: false

            override fun playerName(input: CharSequence): String? {
                return if (detectNot(input)) null
                else Wrapper.getPlayer()?.name?.asString()
            }
        },
        OTHER {
            private val regex = "^<(\\w+)>".toRegex()

            override fun detect(input: CharSequence) = playerName(input) != null

            override fun playerName(input: CharSequence) = Wrapper.getPlayer()?.name?.let { name ->
                regex.find(input)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() && it != name.asString() }
            }
        },
        ANY {
            private val regex = "^<(\\w+)>".toRegex()

            override fun detect(input: CharSequence) = input.contains(regex)

            override fun playerName(input: CharSequence) =
                regex.find(input)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
        };

        override fun removedOrNull(input: CharSequence): CharSequence? = playerName(input)?.let {
            input.removePrefix("<$it>")
        }
    }

    enum class Direct(override vararg val regexes: Regex) : RegexDetector, PlayerDetector {
        SENT("^To (\\w+?): ".toRegex(RegexOption.IGNORE_CASE)),
        RECEIVE(
            "^(\\w+?) whispers( to you)?: ".toRegex(),
            "^\\[?(\\w+?)( )?->( )?\\w+?]?( )?:? ".toRegex(),
            "^From (\\w+?): ".toRegex(RegexOption.IGNORE_CASE),
            "^. (\\w+?) » \\w+? » ".toRegex()
        ),
        ANY(*SENT.regexes, *RECEIVE.regexes);

        override fun playerName(input: CharSequence) = matchedRegex(input)?.let { regex ->
            input.replace(regex, "$1").takeIf { it.isNotBlank() }
        }
    }

    enum class Server(override vararg val regexes: Regex) : RegexDetector {
        QUEUE("^Position in queue: ".toRegex()),
        QUEUE_IMPORTANT("^Position in queue: [1-5]$".toRegex()),
        RESTART("^\\[SERVER] Server restarting in ".toRegex()),
        ANY(*QUEUE.regexes, *RESTART.regexes)
    }

    enum class Other(override vararg val regexes: Regex) : RegexDetector {
        BARITONE("^\\[B(aritone)?]".toRegex()),
        TPA_REQUEST("^\\w+? (has requested|wants) to teleport to you\\.".toRegex())
    }
}
