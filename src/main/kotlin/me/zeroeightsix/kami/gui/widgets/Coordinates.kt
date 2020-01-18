package me.zeroeightsix.kami.gui.widgets

object Coordinates : TextPinnableWidget("Coordinates", text = mutableListOf(
    CompiledText(listOf(
        CompiledText.LiteralPart("x"),
        CompiledText.VariablePart(getVariable("x"), extraspace = false)
    )),
    CompiledText(listOf(
        CompiledText.LiteralPart("y"),
        CompiledText.VariablePart(getVariable("y"), extraspace = false)
    )),
    CompiledText(listOf(
        CompiledText.LiteralPart("z"),
        CompiledText.VariablePart(getVariable("z"), extraspace = false)
    ))
)) {
}