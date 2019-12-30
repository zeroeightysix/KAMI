package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui.sliderInt
import imgui.ImGui.text
import imgui.dsl.menu
import me.zeroeightsix.kami.util.Wrapper
import kotlin.reflect.KMutableProperty0

object Coordinates : PinnableWidget("Coordinates") {

    var digits = 2;

    override fun fillWindow(open: KMutableProperty0<Boolean>) {
        text("x ${Wrapper.getMinecraft().player.pos.x.format(digits)}")
        text("y ${Wrapper.getMinecraft().player.pos.y.format(digits)}")
        text("z ${Wrapper.getMinecraft().player.pos.z.format(digits)}")
    }

    override fun fillContextMenu() = menu("Style") {
        sliderInt("Digits", ::digits, 0, 6)
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

}