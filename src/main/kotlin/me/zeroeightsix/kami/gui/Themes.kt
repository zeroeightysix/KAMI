package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.flag.ImGuiCol
import me.zeroeightsix.kami.gui.ImguiDSL.colors

object Themes {

    enum class Variants(val applyStyle: (Boolean) -> Unit) {
        CLASSIC({ resetSizes ->
            if (resetSizes) {
                resetSizes()
            }
            ImGui.styleColorsClassic()
        }),
        DARK({ resetSizes ->
            if (resetSizes) {
                resetSizes()
            }
            ImGui.styleColorsDark()
        }),
        LIGHT({ resetSizes ->
            if (resetSizes) {
                resetSizes()
            }
            ImGui.styleColorsLight()
        }),
        AQUA({ styleColorsAqua(it) }),
        INFERNO({ styleColorsInferno(it) }),
        CHERRY({ styleColorsCherry(it) })
    }

    private fun resetSizes() {
        with(ImGui.getStyle()) {
            windowRounding = 7f
            windowBorderSize = 1f
            frameRounding = 0f
            grabRounding = 0f
            childRounding = 0f
            frameBorderSize = 0f
        }
    }

    private fun styleColorsAqua(resetSizes: Boolean) {
        val colors = ImGui.getStyle().colors
        colors[ImGuiCol.Text] = floatArrayOf(0.80f, 0.78f, 0.78f, 1.00f)
        colors[ImGuiCol.TextDisabled] = floatArrayOf(0.39f, 0.39f, 0.39f, 1.00f)
        colors[ImGuiCol.WindowBg] = floatArrayOf(0.16f, 0.16f, 0.16f, 0.70f)
        colors[ImGuiCol.ChildBg] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.00f)
        colors[ImGuiCol.PopupBg] = floatArrayOf(0.11f, 0.11f, 0.14f, 0.92f)
        colors[ImGuiCol.Border] = floatArrayOf(0.60f, 0.87f, 0.97f, 0.26f)
        colors[ImGuiCol.BorderShadow] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.00f)
        colors[ImGuiCol.FrameBg] = floatArrayOf(0.63f, 0.50f, 0.50f, 0.12f)
        colors[ImGuiCol.FrameBgHovered] = floatArrayOf(0.69f, 0.59f, 0.59f, 0.12f)
        colors[ImGuiCol.FrameBgActive] = floatArrayOf(0.80f, 0.71f, 0.71f, 0.12f)
        colors[ImGuiCol.TitleBg] = floatArrayOf(0.45f, 0.85f, 0.99f, 0.62f)
        colors[ImGuiCol.TitleBgActive] = floatArrayOf(0.25f, 0.80f, 1.00f, 0.62f)
        colors[ImGuiCol.TitleBgCollapsed] = floatArrayOf(0.45f, 0.68f, 0.78f, 0.20f)
        colors[ImGuiCol.MenuBarBg] = floatArrayOf(0.35f, 0.54f, 0.61f, 0.62f)
        colors[ImGuiCol.ScrollbarBg] = floatArrayOf(0.20f, 0.30f, 0.27f, 0.60f)
        colors[ImGuiCol.ScrollbarGrab] = floatArrayOf(0.40f, 0.67f, 0.80f, 0.30f)
        colors[ImGuiCol.ScrollbarGrabHovered] = floatArrayOf(0.40f, 0.67f, 0.80f, 0.40f)
        colors[ImGuiCol.ScrollbarGrabActive] = floatArrayOf(0.39f, 0.69f, 0.80f, 0.60f)
        colors[ImGuiCol.CheckMark] = floatArrayOf(0.90f, 0.90f, 0.90f, 0.50f)
        colors[ImGuiCol.SliderGrab] = floatArrayOf(1.00f, 1.00f, 1.00f, 0.30f)
        colors[ImGuiCol.SliderGrabActive] = floatArrayOf(0.39f, 0.76f, 0.80f, 0.60f)
        colors[ImGuiCol.Button] = floatArrayOf(0.35f, 0.54f, 0.61f, 0.62f)
        colors[ImGuiCol.ButtonHovered] = floatArrayOf(0.43f, 0.62f, 0.69f, 0.62f)
        colors[ImGuiCol.ButtonActive] = floatArrayOf(0.31f, 0.64f, 0.76f, 0.62f)
        colors[ImGuiCol.Header] = floatArrayOf(0.14f, 0.53f, 0.67f, 0.62f)
        colors[ImGuiCol.HeaderHovered] = floatArrayOf(0.20f, 0.48f, 0.67f, 0.62f)
        colors[ImGuiCol.HeaderActive] = floatArrayOf(0.15f, 0.51f, 0.75f, 0.62f)
        colors[ImGuiCol.Separator] = floatArrayOf(0.50f, 0.50f, 0.50f, 0.60f)
        colors[ImGuiCol.SeparatorHovered] = floatArrayOf(0.60f, 0.60f, 0.70f, 1.00f)
        colors[ImGuiCol.SeparatorActive] = floatArrayOf(0.70f, 0.70f, 0.90f, 1.00f)
        colors[ImGuiCol.ResizeGrip] = floatArrayOf(1.00f, 1.00f, 1.00f, 0.16f)
        colors[ImGuiCol.ResizeGripHovered] = floatArrayOf(0.78f, 1.00f, 1.00f, 0.60f)
        colors[ImGuiCol.ResizeGripActive] = floatArrayOf(0.78f, 0.97f, 1.00f, 0.90f)
        colors[ImGuiCol.Tab] = floatArrayOf(0.14f, 0.53f, 0.67f, 0.62f)
        colors[ImGuiCol.TabHovered] = floatArrayOf(0.22f, 0.61f, 0.75f, 0.62f)
        colors[ImGuiCol.TabActive] = floatArrayOf(0.24f, 0.72f, 0.89f, 0.62f)
        colors[ImGuiCol.TabUnfocused] = floatArrayOf(0.10f, 0.36f, 0.46f, 0.62f)
        colors[ImGuiCol.TabUnfocusedActive] = floatArrayOf(0.13f, 0.46f, 0.58f, 0.62f)
        colors[ImGuiCol.PlotLines] = floatArrayOf(1.00f, 1.00f, 1.00f, 1.00f)
        colors[ImGuiCol.PlotLinesHovered] = floatArrayOf(0.90f, 0.70f, 0.00f, 1.00f)
        colors[ImGuiCol.PlotHistogram] = floatArrayOf(0.90f, 0.70f, 0.00f, 1.00f)
        colors[ImGuiCol.PlotHistogramHovered] = floatArrayOf(1.00f, 0.60f, 0.00f, 1.00f)
        colors[ImGuiCol.TextSelectedBg] = floatArrayOf(0.00f, 0.60f, 1.00f, 0.35f)
        colors[ImGuiCol.DragDropTarget] = floatArrayOf(1.00f, 1.00f, 0.00f, 0.90f)
        colors[ImGuiCol.NavHighlight] = floatArrayOf(0.45f, 0.45f, 0.90f, 0.80f)
        colors[ImGuiCol.NavWindowingHighlight] = floatArrayOf(1.00f, 1.00f, 1.00f, 0.70f)
        colors[ImGuiCol.NavWindowingDimBg] = floatArrayOf(0.80f, 0.80f, 0.80f, 0.20f)
        colors[ImGuiCol.ModalWindowDimBg] = floatArrayOf(0.20f, 0.20f, 0.20f, 0.35f)

        ImGui.getStyle().colors = colors
        if (resetSizes) {
            resetSizes()
            ImGui.getStyle().windowBorderSize = 0f
        }
    }

    private fun styleColorsInferno(resetSizes: Boolean) {
        val colors = ImGui.getStyle().colors
        colors[ImGuiCol.Text] = floatArrayOf(1.00f, 0.96f, 0.96f, 1.00f)
        colors[ImGuiCol.TextDisabled] = floatArrayOf(0.50f, 0.50f, 0.50f, 1.00f)
        colors[ImGuiCol.WindowBg] = floatArrayOf(0.08f, 0.08f, 0.08f, 0.94f)
        colors[ImGuiCol.ChildBg] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.00f)
        colors[ImGuiCol.PopupBg] = floatArrayOf(0.08f, 0.08f, 0.08f, 0.94f)
        colors[ImGuiCol.Border] = floatArrayOf(0.48f, 0.37f, 0.37f, 0.28f)
        colors[ImGuiCol.BorderShadow] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.00f)
        colors[ImGuiCol.FrameBg] = floatArrayOf(0.48f, 0.18f, 0.16f, 0.46f)
        colors[ImGuiCol.FrameBgHovered] = floatArrayOf(0.57f, 0.31f, 0.29f, 0.54f)
        colors[ImGuiCol.FrameBgActive] = floatArrayOf(0.64f, 0.42f, 0.40f, 0.54f)
        colors[ImGuiCol.TitleBg] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.90f)
        colors[ImGuiCol.TitleBgActive] = floatArrayOf(0.48f, 0.19f, 0.16f, 0.88f)
        colors[ImGuiCol.TitleBgCollapsed] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.51f)
        colors[ImGuiCol.MenuBarBg] = floatArrayOf(0.14f, 0.14f, 0.14f, 1.00f)
        colors[ImGuiCol.ScrollbarBg] = floatArrayOf(0.02f, 0.02f, 0.02f, 0.53f)
        colors[ImGuiCol.ScrollbarGrab] = floatArrayOf(0.31f, 0.31f, 0.31f, 1.00f)
        colors[ImGuiCol.ScrollbarGrabHovered] = floatArrayOf(0.41f, 0.41f, 0.41f, 1.00f)
        colors[ImGuiCol.ScrollbarGrabActive] = floatArrayOf(0.51f, 0.51f, 0.51f, 1.00f)
        colors[ImGuiCol.CheckMark] = floatArrayOf(0.63f, 0.39f, 0.34f, 1.00f)
        colors[ImGuiCol.SliderGrab] = floatArrayOf(0.63f, 0.39f, 0.34f, 1.00f)
        colors[ImGuiCol.SliderGrabActive] = floatArrayOf(0.73f, 0.30f, 0.27f, 1.00f)
        colors[ImGuiCol.Button] = floatArrayOf(0.54f, 0.28f, 0.26f, 0.54f)
        colors[ImGuiCol.ButtonHovered] = floatArrayOf(0.53f, 0.34f, 0.33f, 0.54f)
        colors[ImGuiCol.ButtonActive] = floatArrayOf(0.54f, 0.40f, 0.38f, 0.54f)
        colors[ImGuiCol.Header] = floatArrayOf(0.65f, 0.27f, 0.24f, 0.54f)
        colors[ImGuiCol.HeaderHovered] = floatArrayOf(0.74f, 0.35f, 0.32f, 0.54f)
        colors[ImGuiCol.HeaderActive] = floatArrayOf(0.66f, 0.34f, 0.31f, 0.54f)
        colors[ImGuiCol.Separator] = floatArrayOf(0.43f, 0.43f, 0.50f, 0.50f)
        colors[ImGuiCol.SeparatorHovered] = floatArrayOf(0.51f, 0.37f, 0.37f, 0.50f)
        colors[ImGuiCol.SeparatorActive] = floatArrayOf(0.57f, 0.25f, 0.25f, 1.00f)
        colors[ImGuiCol.ResizeGrip] = floatArrayOf(0.98f, 0.29f, 0.26f, 0.25f)
        colors[ImGuiCol.ResizeGripHovered] = floatArrayOf(0.98f, 0.51f, 0.49f, 0.25f)
        colors[ImGuiCol.ResizeGripActive] = floatArrayOf(0.75f, 0.20f, 0.18f, 0.25f)
        colors[ImGuiCol.Tab] = floatArrayOf(0.54f, 0.28f, 0.26f, 0.54f)
        colors[ImGuiCol.TabHovered] = floatArrayOf(0.69f, 0.33f, 0.31f, 0.54f)
        colors[ImGuiCol.TabActive] = floatArrayOf(0.63f, 0.18f, 0.14f, 0.54f)
        colors[ImGuiCol.TabUnfocused] = floatArrayOf(0.15f, 0.07f, 0.07f, 0.97f)
        colors[ImGuiCol.TabUnfocusedActive] = floatArrayOf(0.42f, 0.14f, 0.14f, 1.00f)
        colors[ImGuiCol.PlotLines] = floatArrayOf(0.61f, 0.61f, 0.61f, 1.00f)
        colors[ImGuiCol.PlotLinesHovered] = floatArrayOf(1.00f, 0.43f, 0.35f, 1.00f)
        colors[ImGuiCol.PlotHistogram] = floatArrayOf(0.90f, 0.70f, 0.00f, 1.00f)
        colors[ImGuiCol.PlotHistogramHovered] = floatArrayOf(1.00f, 0.60f, 0.00f, 1.00f)
        colors[ImGuiCol.TextSelectedBg] = floatArrayOf(0.98f, 0.26f, 0.26f, 0.35f)
        colors[ImGuiCol.DragDropTarget] = floatArrayOf(1.00f, 1.00f, 0.00f, 0.90f)
        colors[ImGuiCol.NavHighlight] = floatArrayOf(0.26f, 0.59f, 0.98f, 1.00f)
        colors[ImGuiCol.NavWindowingHighlight] = floatArrayOf(1.00f, 1.00f, 1.00f, 0.70f)
        colors[ImGuiCol.NavWindowingDimBg] = floatArrayOf(0.80f, 0.80f, 0.80f, 0.20f)
        colors[ImGuiCol.ModalWindowDimBg] = floatArrayOf(0.80f, 0.80f, 0.80f, 0.35f)

        ImGui.getStyle().colors = colors
        if (resetSizes) {
            resetSizes()
            with(ImGui.getStyle()) {
                windowRounding = 9f
                frameRounding = 4f
                grabRounding = 4f
                childRounding = 3f
                frameBorderSize = 1f
            }
        }
    }

    private fun styleColorsCherry(resetSizes: Boolean) {
        val colors = ImGui.getStyle().colors
        colors[ImGuiCol.Text] = floatArrayOf(0.86f, 0.93f, 0.89f, 0.78f)
        colors[ImGuiCol.TextDisabled] = floatArrayOf(0.86f, 0.93f, 0.89f, 0.28f)
        colors[ImGuiCol.WindowBg] = floatArrayOf(0.13f, 0.14f, 0.17f, 1.00f)
        colors[ImGuiCol.ChildBg] = floatArrayOf(0.20f, 0.22f, 0.27f, 0.58f)
        colors[ImGuiCol.PopupBg] = floatArrayOf(0.20f, 0.22f, 0.27f, 0.9f)
        colors[ImGuiCol.Border] = floatArrayOf(0.31f, 0.31f, 1.00f, 0.00f)
        colors[ImGuiCol.BorderShadow] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.00f)
        colors[ImGuiCol.FrameBg] = floatArrayOf(0.20f, 0.22f, 0.27f, 1.00f)
        colors[ImGuiCol.FrameBgHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.78f)
        colors[ImGuiCol.FrameBgActive] = floatArrayOf(0.46f, 0.12f, 0.30f, 1.00f)
        colors[ImGuiCol.TitleBg] = floatArrayOf(0.23f, 0.20f, 0.27f, 1.00f)
        colors[ImGuiCol.TitleBgActive] = floatArrayOf(0.50f, 0.08f, 0.26f, 1.00f)
        colors[ImGuiCol.TitleBgCollapsed] = floatArrayOf(0.20f, 0.22f, 0.27f, 0.75f)
        colors[ImGuiCol.MenuBarBg] = floatArrayOf(0.20f, 0.22f, 0.27f, 0.47f)
        colors[ImGuiCol.ScrollbarBg] = floatArrayOf(0.20f, 0.22f, 0.27f, 1.00f)
        colors[ImGuiCol.ScrollbarGrab] = floatArrayOf(0.09f, 0.15f, 0.16f, 1.00f)
        colors[ImGuiCol.ScrollbarGrabHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.78f)
        colors[ImGuiCol.ScrollbarGrabActive] = floatArrayOf(0.46f, 0.12f, 0.30f, 1.00f)
        colors[ImGuiCol.CheckMark] = floatArrayOf(0.71f, 0.22f, 0.27f, 1.00f)
        colors[ImGuiCol.SliderGrab] = floatArrayOf(0.47f, 0.77f, 0.83f, 0.14f)
        colors[ImGuiCol.SliderGrabActive] = floatArrayOf(0.71f, 0.22f, 0.27f, 1.00f)
        colors[ImGuiCol.Button] = floatArrayOf(0.47f, 0.77f, 0.83f, 0.14f)
        colors[ImGuiCol.ButtonHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.86f)
        colors[ImGuiCol.ButtonActive] = floatArrayOf(0.46f, 0.12f, 0.30f, 1.00f)
        colors[ImGuiCol.Header] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.76f)
        colors[ImGuiCol.HeaderHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.86f)
        colors[ImGuiCol.HeaderActive] = floatArrayOf(0.50f, 0.08f, 0.26f, 1.00f)
        colors[ImGuiCol.Separator] = floatArrayOf(0.14f, 0.16f, 0.19f, 1.00f)
        colors[ImGuiCol.SeparatorHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.78f)
        colors[ImGuiCol.SeparatorActive] = floatArrayOf(0.46f, 0.12f, 0.30f, 1.00f)
        colors[ImGuiCol.ResizeGrip] = floatArrayOf(0.47f, 0.77f, 0.83f, 0.04f)
        colors[ImGuiCol.ResizeGripHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.78f)
        colors[ImGuiCol.ResizeGripActive] = floatArrayOf(0.46f, 0.12f, 0.30f, 1.00f)
        colors[ImGuiCol.PlotLines] = floatArrayOf(0.86f, 0.93f, 0.89f, 0.63f)
        colors[ImGuiCol.PlotLinesHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 1.00f)
        colors[ImGuiCol.PlotHistogram] = floatArrayOf(0.86f, 0.93f, 0.89f, 0.63f)
        colors[ImGuiCol.PlotHistogramHovered] = floatArrayOf(0.46f, 0.12f, 0.30f, 1.00f)
        colors[ImGuiCol.TextSelectedBg] = floatArrayOf(0.46f, 0.12f, 0.30f, 0.43f)
        colors[ImGuiCol.Tab] = colors[ImGuiCol.TitleBgActive]
        colors[ImGuiCol.TabHovered] = colors[ImGuiCol.HeaderHovered]
        colors[ImGuiCol.TabActive] = colors[ImGuiCol.TitleBgActive]
        colors[ImGuiCol.TabUnfocused] = colors[ImGuiCol.TitleBg]
        colors[ImGuiCol.TabUnfocusedActive] = colors[ImGuiCol.TitleBg]
        colors[ImGuiCol.NavHighlight] = colors[ImGuiCol.HeaderHovered]
        colors[ImGuiCol.ModalWindowDimBg] = floatArrayOf(0.00f, 0.00f, 0.00f, 0.32f)
        // @formatter:on

        ImGui.getStyle().colors = colors
        if (resetSizes) {
            resetSizes()
        }
    }
}
