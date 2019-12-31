package me.zeroeightsix.kami.gui.windows

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui.closeCurrentPopup
import imgui.ImGui.combo
import imgui.ImGui.openPopup
import imgui.ImGui.sameLine
import imgui.ImGui.setItemDefaultFocus
import imgui.ImGui.showDemoWindow
import imgui.ImGui.styleColorsClassic
import imgui.ImGui.styleColorsDark
import imgui.ImGui.styleColorsLight
import imgui.WindowFlag
import imgui.api.g
import imgui.dsl.button
import imgui.dsl.checkbox
import imgui.dsl.menu
import imgui.dsl.menuBar
import imgui.dsl.menuItem
import imgui.dsl.popupModal
import imgui.dsl.window
import imgui.internal.lerp

object KamiDebugWindow {
    
    var showDemoWindow = false
    var styleIdx = 0
    var oldTheme = styleIdx
    var openPopupNext = false
    val themes = listOf("Classic", "Dark", "Light", "Cherry")

    operator fun invoke() = window("Kami debug", flags = WindowFlag.MenuBar.i) {
        menuBar {
            menu("Theme") {
                menuItem("Pick") {
                    oldTheme = styleIdx
                    openPopupNext = true // Because openPopup doesn't do its job here, do it later
                }
            }
        }

        if (openPopupNext) {
            openPopup("Pick a theme")
            openPopupNext = false
        }

        popupModal("Pick a theme", null, WindowFlag.AlwaysAutoResize.i) {
            if (combo("Theme", ::styleIdx, themes)) {
                when (styleIdx) {
                    0 -> styleColorsClassic()
                    1 -> styleColorsDark()
                    2 -> styleColorsLight()
                    3 -> styleColorsCherry()
                }
            }
            button("OK", Vec2(120, 0)) {
                closeCurrentPopup()
            }
            setItemDefaultFocus()
            sameLine()
            button("Cancel", Vec2(120, 0)) {
                styleIdx = oldTheme
                when (styleIdx) {
                    0 -> styleColorsClassic()
                    1 -> styleColorsDark()
                    2 -> styleColorsLight()
                    3 -> styleColorsCherry()
                }
                closeCurrentPopup()
            }
        }

        checkbox("Show demo window", ::showDemoWindow) {}
        if (showDemoWindow) {
            showDemoWindow(::showDemoWindow)
        }
    }

    private fun styleColorsCherry() {
        with (g.style) {
            colors.clear()
            for (c in Col.values()) colors += Vec4()
            // @formatter:off
            colors[Col.Text]                  (0.86f, 0.93f, 0.89f, 0.78f)
            colors[Col.TextDisabled]          (0.86f, 0.93f, 0.89f, 0.28f)
            colors[Col.WindowBg]              (0.13f, 0.14f, 0.17f, 1.00f)
            colors[Col.ChildBg]               (0.20f, 0.22f, 0.27f, 0.58f)
            colors[Col.PopupBg]               (0.20f, 0.22f, 0.27f, 0.9f)
            colors[Col.Border]                (0.31f, 0.31f, 1.00f, 0.00f)
            colors[Col.BorderShadow]          (0.00f, 0.00f, 0.00f, 0.00f)
            colors[Col.FrameBg]               (0.20f, 0.22f, 0.27f, 1.00f)
            colors[Col.FrameBgHovered]        (0.46f, 0.12f, 0.30f, 0.78f)
            colors[Col.FrameBgActive]         (0.46f, 0.12f, 0.30f, 1.00f)
            colors[Col.TitleBg]               (0.23f, 0.20f, 0.27f, 1.00f)
            colors[Col.TitleBgActive]         (0.50f, 0.08f, 0.26f, 1.00f)
            colors[Col.TitleBgCollapsed]      (0.20f, 0.22f, 0.27f, 0.75f)
            colors[Col.MenuBarBg]             (0.20f, 0.22f, 0.27f, 0.47f)
            colors[Col.ScrollbarBg]           (0.20f, 0.22f, 0.27f, 1.00f)
            colors[Col.ScrollbarGrab]         (0.09f, 0.15f, 0.16f, 1.00f)
            colors[Col.ScrollbarGrabHovered]  (0.46f, 0.12f, 0.30f, 0.78f)
            colors[Col.ScrollbarGrabActive]   (0.46f, 0.12f, 0.30f, 1.00f)
            colors[Col.CheckMark]             (0.71f, 0.22f, 0.27f, 1.00f)
            colors[Col.SliderGrab]            (0.47f, 0.77f, 0.83f, 0.14f)
            colors[Col.SliderGrabActive]      (0.71f, 0.22f, 0.27f, 1.00f)
            colors[Col.Button]                (0.47f, 0.77f, 0.83f, 0.14f)
            colors[Col.ButtonHovered]         (0.46f, 0.12f, 0.30f, 0.86f)
            colors[Col.ButtonActive]          (0.46f, 0.12f, 0.30f, 1.00f)
            colors[Col.Header]                (0.46f, 0.12f, 0.30f, 0.76f)
            colors[Col.HeaderHovered]         (0.46f, 0.12f, 0.30f, 0.86f)
            colors[Col.HeaderActive]          (0.50f, 0.08f, 0.26f, 1.00f)
            colors[Col.Separator]             (0.14f, 0.16f, 0.19f, 1.00f)
            colors[Col.SeparatorHovered]      (0.46f, 0.12f, 0.30f, 0.78f)
            colors[Col.SeparatorActive]       (0.46f, 0.12f, 0.30f, 1.00f)
            colors[Col.ResizeGrip]            (0.47f, 0.77f, 0.83f, 0.04f)
            colors[Col.ResizeGripHovered]     (0.46f, 0.12f, 0.30f, 0.78f)
            colors[Col.ResizeGripActive]      (0.46f, 0.12f, 0.30f, 1.00f)
            colors[Col.PlotLines]             (0.86f, 0.93f, 0.89f, 0.63f)
            colors[Col.PlotLinesHovered]      (0.46f, 0.12f, 0.30f, 1.00f)
            colors[Col.PlotHistogram]         (0.86f, 0.93f, 0.89f, 0.63f)
            colors[Col.PlotHistogramHovered]  (0.46f, 0.12f, 0.30f, 1.00f)
            colors[Col.TextSelectedBg]        (0.46f, 0.12f, 0.30f, 0.43f)
            colors[Col.Tab]                   (colors[Col.Header].lerp(colors[Col.TitleBgActive], 0.90f))
            colors[Col.TabHovered]            (colors[Col.HeaderHovered])
            colors[Col.TabActive]             (colors[Col.HeaderActive].lerp(colors[Col.TitleBgActive], 0.60f))
            colors[Col.TabUnfocused]          (colors[Col.Tab].lerp(colors[Col.TitleBg], 0.80f))
            colors[Col.TabUnfocusedActive]    (colors[Col.TabActive].lerp(colors[Col.TitleBg], 0.40f))
            colors[Col.NavHighlight]          (colors[Col.HeaderHovered])
            colors[Col.ModalWindowDimBg]      (0.00f, 0.00f, 0.00f, 0.32f)
            // @formatter:on
        }
    }

    operator fun <T> ArrayList<T>.get(col: Col): T = get(col.i)

}