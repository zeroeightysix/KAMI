package me.zeroeightsix.kami.gui.widgets

object Coordinates : TextPinnableWidget("Coordinates", text = mutableListOf(
    CompiledText(mutableListOf(
        CompiledText.LiteralPart("x"),
        CompiledText.VariablePart(getVariable("x"), extraspace = false)
    )),
    CompiledText(mutableListOf(
        CompiledText.LiteralPart("y"),
        CompiledText.VariablePart(getVariable("y"), extraspace = false)
    )),
    CompiledText(mutableListOf(
        CompiledText.LiteralPart("z"),
        CompiledText.VariablePart(getVariable("z"), extraspace = false)
    ))
), position = Position.BOTTOM_LEFT)