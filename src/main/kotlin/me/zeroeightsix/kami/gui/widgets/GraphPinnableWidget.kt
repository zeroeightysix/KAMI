package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.ImGui
import imgui.dsl
import imgui.impl.time
import me.zeroeightsix.kami.mixin.client.IMinecraftClient
import me.zeroeightsix.kami.setting.GenerateType
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
    var _capacity: Int = (sampleRate * 10).roundToInt()
) : PinnableWidget(name, position, open, pinned, background) {

    init {
        // The user can resize graph widgets as they wish
        this.autoResize = false
    }

    var refreshTime = 0.0

    var capacity = _capacity
        set(value) {
            field = value
            // In case the capacity shrunk, remove those elements
            while (samples.size > value)
                samples.removeLast()
        }

    var samples = ArrayList<Float>(capacity)

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
