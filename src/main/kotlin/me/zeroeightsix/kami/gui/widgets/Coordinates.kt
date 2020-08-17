package me.zeroeightsix.kami.gui.widgets

object Coordinates : TextPinnableWidget(
    "Coordinates", text = mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.LiteralPart("x"),
                CompiledText.VariablePart(varMap["x"]!!(), extraspace = false)
            )
        ),
        CompiledText(
            mutableListOf(
                CompiledText.LiteralPart("y"),
                CompiledText.VariablePart(varMap["y"]!!(), extraspace = false)
            )
        ),
        CompiledText(
            mutableListOf(
                CompiledText.LiteralPart("z"),
                CompiledText.VariablePart(varMap["z"]!!(), extraspace = false)
            )
        )
    ), position = Position.BOTTOM_LEFT
)
