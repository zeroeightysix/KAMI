package me.zeroeightsix.kami.gui.windows

import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui
import imgui.ImGui.dragFloat
import imgui.ImGui.sameLine
import imgui.api.demoDebugInformations
import imgui.api.g
import imgui.dsl.checkbox
import imgui.dsl.collapsingHeader
import imgui.dsl.window
import imgui.internal.lerp

object KamiSettings {
    
    var settingsWindowOpen = false
    var swapModuleListButtons = false
    var hideModuleDescriptions = false
    var hideModuleMarker = false
    var styleIdx = 0
    var borderOffset = 10f

    private val themes = listOf("Classic", "Dark", "Light", "Cherry")

    operator fun invoke() {
        if (settingsWindowOpen) {
            window("Settings", ::settingsWindowOpen) {
                collapsingHeader("Module windows") {
                    checkbox("Swap list buttons", ::swapModuleListButtons) {}
                    sameLine()
                    demoDebugInformations.helpMarker("When enabled, right clicking modules will reveal their settings menu. Left clicking will toggle the module.")

                    checkbox("Hide descriptions", ::hideModuleDescriptions) {}
                    sameLine()
                    demoDebugInformations.helpMarker("Hide module descriptions when its settings are opened.")

                    checkbox("Hide help marker", ::hideModuleMarker) {}
                    sameLine()
                    demoDebugInformations.helpMarker("Hide the help marker (such as the one you are hovering right now) in module settings.")
                }

                collapsingHeader("GUI") {
                    if (ImGui.combo("Theme", ::styleIdx, themes)) {
                        when (styleIdx) {
                            0 -> ImGui.styleColorsClassic()
                            1 -> ImGui.styleColorsDark()
                            2 -> ImGui.styleColorsLight()
                            3 -> styleColorsCherry()
                        }
                    }
                }

                collapsingHeader("Overlay") {
                    dragFloat("Border offset", ::borderOffset, vMin = 0f, vMax = 50f, format = "%.0f")
                }
            }
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