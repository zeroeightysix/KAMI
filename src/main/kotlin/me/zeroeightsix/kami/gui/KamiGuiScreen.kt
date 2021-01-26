package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.flag.ImGuiCol
import me.zeroeightsix.kami.feature.hidden.PrepHandler
import me.zeroeightsix.kami.gui.ImguiDSL.colors
import me.zeroeightsix.kami.gui.ImguiDSL.wrapImBool
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.gui.wizard.Wizard
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.text
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack

class KamiGuiScreen(private var parent: Screen? = null) : ImGuiScreen(text(null, "Kami GUI")) {
    companion object {
        private val colourIndices = (0 until ImGuiCol.COUNT).toList()

        fun renderGui() {
            if (Settings.rainbowMode) {
                Themes.Variants.values()[Settings.styleIdx].applyStyle(false)
                val colours = ImGui.getStyle().colors
                colourIndices.forEach { idx ->
                    val col = colours[idx]
                    val buf = FloatArray(3)
                    ImGui.colorConvertRGBtoHSV(floatArrayOf(col[0], col[1], col[2]), buf)
                    buf[0] = PrepHandler.getRainbowHue(buf[0].toDouble()).toFloat()
                    ImGui.colorConvertHSVtoRGB(buf, buf)
                    colours[idx] = floatArrayOf(buf[0], buf[1], buf[2], col[3])
                }
                ImGui.getStyle().colors = colours
            }

            // Draw the main menu bar.
            MenuBar()
            // Debug window (theme, demo window)
            if (View.demoWindowVisible) {
                wrapImBool(View::demoWindowVisible) {
                    ImGui.showDemoWindow(it)
                }
            }
            // Draw all module windows
            Modules()
            // Draw the settings
            Settings()

            if (!EnabledWidgets.hideAll) {
                showWidgets()
            }
        }

        fun showWidgets(limitY: Boolean = true) {
            EnabledWidgets.widgets.removeAll {
                it.open && it.showWindow(limitY)
            }
        }
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        if (parent != null) {
            // If we have a screen to return to, draw the background.
            // Usually parent will be nonnull if the GUI was opened from e.g. the title menu, so it would have no
            // background if this render doesn't happen.
            this.renderBackground(matrices)
        }
        super.render(matrices, mouseX, mouseY, delta)

        KamiImgui.frame(matrices!!) {
            if (Wizard()) return@frame

            renderGui()
        }
    }

    override fun onClose() {
        mc.openScreen(this.parent)
    }
}
