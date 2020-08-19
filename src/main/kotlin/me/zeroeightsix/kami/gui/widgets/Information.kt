package me.zeroeightsix.kami.gui.widgets

object Information : TextPinnableWidget(
    "Information",
    mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(varMap["client"]!!()),
                CompiledText.VariablePart(varMap["version"]!!(), extraspace = false)
            )
        ),
        CompiledText(
            mutableListOf(
                CompiledText.LiteralPart("Welcome"),
                CompiledText.VariablePart(varMap["username"]!!(), extraspace = false)
            )
        ),
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(varMap["tps"]!!()),
                CompiledText.LiteralPart("tps", extraspace = false)
            )
        )
    ), position = Position.TOP_LEFT
)
