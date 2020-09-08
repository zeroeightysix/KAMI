package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.Col
import imgui.ImGui
import imgui.WindowFlag
import imgui.dsl
import imgui.impl.time
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.mixin.client.IMinecraftClient
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.setting.settingInterface
import kotlin.math.roundToInt

@GenerateType
class GraphPinnableWidget(
    name: String,
    position: Position = Position.TOP_LEFT,
    open: Boolean = true,
    pinned: Boolean = true,
    background: Boolean = false,
    // The sampling rate in Hertz
    var sampleRate: Float = 1f,
    // The amount of samples to keep in the queue
    _capacity: Int = (sampleRate * 10).roundToInt(),
    var variable: TextPinnableWidget.CompiledText.NumericalVariable = TextPinnableWidget.varMap["fps"]!!() as TextPinnableWidget.CompiledText.NumericalVariable,
    var linesColour: Colour = Colour.WHITE,
    var backgroundColour: Colour = Colour.TRANSPARENT
) : PinnableWidget(name, position, open, pinned, background) {

    var capacity = _capacity
        set(value) {
            field = value
            // In case the capacity shrunk, remove those elements
            while (samples.size > value)
                samples.removeLast()
        }

    var edit = false

    private var refreshTime = 0.0

    var samples = ArrayList<Float>(capacity)

    init {
        // The user can resize graph widgets as they wish
        this.autoResize = false
    }

    override fun fillWindow() {
        if (sampleRate != 0f) {
            if (refreshTime == 0.0) refreshTime = time
            while (refreshTime < time) {
                samples.add(0, IMinecraftClient.getCurrentFps().toFloat())
                if (samples.size > capacity)
                    samples.removeLast()
                refreshTime += 1 / sampleRate
            }
        }
        dsl.withStyleColor(Col.PlotLines, linesColour.asVec4()) {
            dsl.withStyleColor(Col.FrameBg, backgroundColour.asVec4()) {
                dsl.withItemWidth(-(ImGui.calcTextSize(name).x + ImGui.style.windowPadding.x)) {
                    ImGui.plotLines(
                        name,
                        { idx -> samples[idx] },
                        samples.size,
                        scaleMin = 0f,
                        graphSize = Vec2(0, ImGui.windowHeight - ImGui.style.windowPadding.y * 2)
                    )
                }
            }
        }
    }

    override fun fillContextMenu() {
        dsl.menuItem("Edit", selected = edit) {
            edit = !edit
        }
    }

    override fun postWindow() {
        // You'd think that `dsl.window` doesn't display the window if edit is false, but it still does. So we just check the value ourselves.
        if (edit) {
            dsl.window("Edit $name", ::edit, WindowFlag.AlwaysAutoResize.i) {
                KamiConfig.colourType.settingInterface?.let { interf ->
                    interf.displayImGui("Line colour", linesColour)?.let { linesColour = it }
                    interf.displayImGui("Background colour", backgroundColour)?.let { backgroundColour = it }
                }
            }
        }
    }
}
