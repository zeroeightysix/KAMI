package me.zeroeightsix.kami.gui.widgets

import me.zeroeightsix.kami.gui.text.CompiledText
import me.zeroeightsix.kami.gui.text.VarMap

object Information : TextPinnableWidget(
    "Information",
    mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(VarMap["client"]!!()),
                CompiledText.VariablePart(VarMap["version"]!!(), extraspace = false)
            )
        ),
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(VarMap["fps"]!!()),
                CompiledText.LiteralPart("fps").also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                },
                CompiledText.VariablePart(VarMap["tps"]!!()),
                CompiledText.LiteralPart("tps", extraspace = false).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                }
            )
        )
    ),
    position = Position.TOP_LEFT
)
