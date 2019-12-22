package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.ImGui.text
import imgui.WindowFlag
import imgui.dsl.popupContextItem
import imgui.dsl.window
import imgui.or
import kotlin.reflect.KMutableProperty0

object Information {

    var pinned = false;

    operator fun invoke(open: KMutableProperty0<Boolean>) {
        var flags = WindowFlag.NoDecoration or WindowFlag.AlwaysAutoResize or WindowFlag.NoSavedSettings or WindowFlag.NoFocusOnAppearing or WindowFlag.NoNav
        window("Information", open, flags) {
            text("Welcome to KAMI!")
            popupContextItem("information context menu") {
                ImGui.checkbox("Pinned", ::pinned)
            }
        }
    }

}