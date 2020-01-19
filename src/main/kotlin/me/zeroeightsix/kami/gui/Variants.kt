package me.zeroeightsix.kami.gui

import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui
import imgui.api.g
import imgui.internal.lerp
import me.zeroeightsix.kami.gui.windows.KamiSettings.get

object Themes {

    enum class Variants(val applyStyle: () -> Unit) {
        CLASSIC({ ImGui.styleColorsClassic() }),
        DARK({ ImGui.styleColorsDark() }),
        LIGHT({ ImGui.styleColorsLight() }),
        AQUA({ styleColorsAqua() }),
        INFERNO({ styleColorsInferno()}),
        CHERRY({ styleColorsCherry() })
    }
    
    private fun styleColorsAqua() {
        val colors = ImGui.style.colors
        colors[Col.Text.i] = Vec4(0.80, 0.78, 0.78, 1.00)
        colors[Col.TextDisabled.i] = Vec4(0.39, 0.39, 0.39, 1.00)
        colors[Col.WindowBg.i] = Vec4(0.16, 0.16, 0.16, 0.70)
        colors[Col.ChildBg.i] = Vec4(0.00, 0.00, 0.00, 0.00)
        colors[Col.PopupBg.i] = Vec4(0.11, 0.11, 0.14, 0.92)
        colors[Col.Border.i] = Vec4(0.31, 0.33, 0.74, 0.50)
        colors[Col.BorderShadow.i] = Vec4(0.00, 0.00, 0.00, 0.00)
        colors[Col.FrameBg.i] = Vec4(0.63, 0.50, 0.50, 0.12)
        colors[Col.FrameBgHovered.i] = Vec4(0.47, 0.47, 0.69, 0.40)
        colors[Col.FrameBgActive.i] = Vec4(0.42, 0.41, 0.64, 0.69)
        colors[Col.TitleBg.i] = Vec4(0.27, 0.27, 0.54, 0.83)
        colors[Col.TitleBgActive.i] = Vec4(0.32, 0.32, 0.63, 0.87)
        colors[Col.TitleBgCollapsed.i] = Vec4(0.40, 0.40, 0.80, 0.20)
        colors[Col.MenuBarBg.i] = Vec4(0.40, 0.40, 0.55, 0.80)
        colors[Col.ScrollbarBg.i] = Vec4(0.20, 0.25, 0.30, 0.60)
        colors[Col.ScrollbarGrab.i] = Vec4(0.40, 0.40, 0.80, 0.30)
        colors[Col.ScrollbarGrabHovered.i] = Vec4(0.40, 0.40, 0.80, 0.40)
        colors[Col.ScrollbarGrabActive.i] = Vec4(0.41, 0.39, 0.80, 0.60)
        colors[Col.CheckMark.i] = Vec4(0.90, 0.90, 0.90, 0.50)
        colors[Col.SliderGrab.i] = Vec4(1.00, 1.00, 1.00, 0.30)
        colors[Col.SliderGrabActive.i] = Vec4(0.41, 0.39, 0.80, 0.60)
        colors[Col.Button.i] = Vec4(0.35, 0.40, 0.61, 0.62)
        colors[Col.ButtonHovered.i] = Vec4(0.40, 0.48, 0.71, 0.79)
        colors[Col.ButtonActive.i] = Vec4(0.46, 0.54, 0.80, 1.00)
        colors[Col.Header.i] = Vec4(0.40, 0.40, 0.90, 0.45)
        colors[Col.HeaderHovered.i] = Vec4(0.45, 0.45, 0.90, 0.80)
        colors[Col.HeaderActive.i] = Vec4(0.53, 0.53, 0.87, 0.80)
        colors[Col.Separator.i] = Vec4(0.50, 0.50, 0.50, 0.60)
        colors[Col.SeparatorHovered.i] = Vec4(0.60, 0.60, 0.70, 1.00)
        colors[Col.SeparatorActive.i] = Vec4(0.70, 0.70, 0.90, 1.00)
        colors[Col.ResizeGrip.i] = Vec4(1.00, 1.00, 1.00, 0.16)
        colors[Col.ResizeGripHovered.i] = Vec4(0.78, 0.82, 1.00, 0.60)
        colors[Col.ResizeGripActive.i] = Vec4(0.78, 0.82, 1.00, 0.90)
        colors[Col.Tab.i] = Vec4(0.34, 0.34, 0.68, 0.79)
        colors[Col.TabHovered.i] = Vec4(0.45, 0.45, 0.90, 0.80)
        colors[Col.TabActive.i] = Vec4(0.40, 0.40, 0.73, 0.84)
        colors[Col.TabUnfocused.i] = Vec4(0.28, 0.28, 0.57, 0.82)
        colors[Col.TabUnfocusedActive.i] = Vec4(0.35, 0.35, 0.65, 0.84)
        colors[Col.PlotLines.i] = Vec4(1.00, 1.00, 1.00, 1.00)
        colors[Col.PlotLinesHovered.i] = Vec4(0.90, 0.70, 0.00, 1.00)
        colors[Col.PlotHistogram.i] = Vec4(0.90, 0.70, 0.00, 1.00)
        colors[Col.PlotHistogramHovered.i] = Vec4(1.00, 0.60, 0.00, 1.00)
        colors[Col.TextSelectedBg.i] = Vec4(0.00, 0.00, 1.00, 0.35)
        colors[Col.DragDropTarget.i] = Vec4(1.00, 1.00, 0.00, 0.90)
        colors[Col.NavHighlight.i] = Vec4(0.45, 0.45, 0.90, 0.80)
        colors[Col.NavWindowingHighlight.i] = Vec4(1.00, 1.00, 1.00, 0.70)
        colors[Col.NavWindowingDimBg.i] = Vec4(0.80, 0.80, 0.80, 0.20)
        colors[Col.ModalWindowDimBg.i] = Vec4(0.20, 0.20, 0.20, 0.35)
    }
    
    private fun styleColorsInferno() {
        val colors = ImGui.style.colors
        colors[Col.Text.i] = Vec4(1.00, 0.96, 0.96, 1.00)
        colors[Col.TextDisabled.i] = Vec4(0.50, 0.50, 0.50, 1.00)
        colors[Col.WindowBg.i] = Vec4(0.08, 0.08, 0.08, 0.94)
        colors[Col.ChildBg.i] = Vec4(0.00, 0.00, 0.00, 0.00)
        colors[Col.PopupBg.i] = Vec4(0.08, 0.08, 0.08, 0.94)
        colors[Col.Border.i] = Vec4(0.00, 0.00, 0.00, 0.50)
        colors[Col.BorderShadow.i] = Vec4(0.00, 0.00, 0.00, 0.00)
        colors[Col.FrameBg.i] = Vec4(0.48, 0.18, 0.16, 0.54)
        colors[Col.FrameBgHovered.i] = Vec4(0.57, 0.31, 0.29, 0.54)
        colors[Col.FrameBgActive.i] = Vec4(0.64, 0.42, 0.40, 0.54)
        colors[Col.TitleBg.i] = Vec4(0.04, 0.04, 0.04, 1.00)
        colors[Col.TitleBgActive.i] = Vec4(0.48, 0.19, 0.16, 0.80)
        colors[Col.TitleBgCollapsed.i] = Vec4(0.00, 0.00, 0.00, 0.51)
        colors[Col.MenuBarBg.i] = Vec4(0.14, 0.14, 0.14, 1.00)
        colors[Col.ScrollbarBg.i] = Vec4(0.02, 0.02, 0.02, 0.53)
        colors[Col.ScrollbarGrab.i] = Vec4(0.31, 0.31, 0.31, 1.00)
        colors[Col.ScrollbarGrabHovered.i] = Vec4(0.41, 0.41, 0.41, 1.00)
        colors[Col.ScrollbarGrabActive.i] = Vec4(0.51, 0.51, 0.51, 1.00)
        colors[Col.CheckMark.i] = Vec4(0.63, 0.39, 0.34, 1.00)
        colors[Col.SliderGrab.i] = Vec4(0.63, 0.39, 0.34, 1.00)
        colors[Col.SliderGrabActive.i] = Vec4(0.73, 0.30, 0.27, 1.00)
        colors[Col.Button.i] = Vec4(0.54, 0.28, 0.26, 0.54)
        colors[Col.ButtonHovered.i] = Vec4(0.53, 0.34, 0.33, 0.54)
        colors[Col.ButtonActive.i] = Vec4(0.54, 0.40, 0.38, 0.54)
        colors[Col.Header.i] = Vec4(0.65, 0.27, 0.24, 0.54)
        colors[Col.HeaderHovered.i] = Vec4(0.74, 0.35, 0.32, 0.54)
        colors[Col.HeaderActive.i] = Vec4(0.66, 0.34, 0.31, 0.54)
        colors[Col.Separator.i] = Vec4(0.43, 0.43, 0.50, 0.50)
        colors[Col.SeparatorHovered.i] = Vec4(0.51, 0.37, 0.37, 0.50)
        colors[Col.SeparatorActive.i] = Vec4(0.57, 0.25, 0.25, 1.00)
        colors[Col.ResizeGrip.i] = Vec4(0.98, 0.29, 0.26, 0.25)
        colors[Col.ResizeGripHovered.i] = Vec4(0.98, 0.51, 0.49, 0.25)
        colors[Col.ResizeGripActive.i] = Vec4(0.75, 0.20, 0.18, 0.25)
        colors[Col.Tab.i] = Vec4(0.54, 0.28, 0.26, 0.54)
        colors[Col.TabHovered.i] = Vec4(0.69, 0.33, 0.31, 0.54)
        colors[Col.TabActive.i] = Vec4(0.63, 0.18, 0.14, 0.54)
        colors[Col.TabUnfocused.i] = Vec4(0.15, 0.07, 0.07, 0.97)
        colors[Col.TabUnfocusedActive.i] = Vec4(0.42, 0.14, 0.14, 1.00)
        colors[Col.PlotLines.i] = Vec4(0.61, 0.61, 0.61, 1.00)
        colors[Col.PlotLinesHovered.i] = Vec4(1.00, 0.43, 0.35, 1.00)
        colors[Col.PlotHistogram.i] = Vec4(0.90, 0.70, 0.00, 1.00)
        colors[Col.PlotHistogramHovered.i] = Vec4(1.00, 0.60, 0.00, 1.00)
        colors[Col.TextSelectedBg.i] = Vec4(0.98, 0.26, 0.26, 0.35)
        colors[Col.DragDropTarget.i] = Vec4(1.00, 1.00, 0.00, 0.90)
        colors[Col.NavHighlight.i] = Vec4(0.26, 0.59, 0.98, 1.00)
        colors[Col.NavWindowingHighlight.i] = Vec4(1.00, 1.00, 1.00, 0.70)
        colors[Col.NavWindowingDimBg.i] = Vec4(0.80, 0.80, 0.80, 0.20)
        colors[Col.ModalWindowDimBg.i] = Vec4(0.80, 0.80, 0.80, 0.35)
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

}