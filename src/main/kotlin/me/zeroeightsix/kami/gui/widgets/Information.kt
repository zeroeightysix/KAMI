package me.zeroeightsix.kami.gui.widgets

import me.zeroeightsix.kami.KamiMod

object Information : TextPinnableWidget("Information",
    extendStd(mapOf(
        Pair("version", { CompiledText.ConstantVariable(string = KamiMod.MODVER) }),
        Pair("client", { CompiledText.ConstantVariable(string = KamiMod.MODNAME) }),
        Pair("kanji", { CompiledText.ConstantVariable(string = KamiMod.KAMI_KANJI) })
    )),
    mutableListOf(
        CompiledText(mutableListOf(
            CompiledText.VariablePart(getVariable("client")),
            CompiledText.VariablePart(getVariable("version"), extraspace = false)
        )),
        CompiledText(mutableListOf(
            CompiledText.LiteralPart("Welcome"),
            CompiledText.VariablePart(getVariable("username"), extraspace = false)
        )),
        CompiledText(mutableListOf(
            CompiledText.VariablePart(getVariable("tps")),
            CompiledText.LiteralPart("tps", extraspace = false)
        ))
    ), position = Position.TOP_LEFT)