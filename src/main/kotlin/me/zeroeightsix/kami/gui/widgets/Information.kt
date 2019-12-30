package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui.text
import kotlin.reflect.KMutableProperty0

object Information : PinnableWidget("Information") {

    override fun fillWindow(open: KMutableProperty0<Boolean>) = text("Welcome to KAMI!")

}