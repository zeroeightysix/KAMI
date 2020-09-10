package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.*
import imgui.impl.time
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.setting.settingInterface
import me.zeroeightsix.kami.to
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
    var capacity: Int = (sampleRate * 10).roundToInt(),
    var variable: TextPinnableWidget.CompiledText.NumericalVariable = VarMap["fps"]!!() as TextPinnableWidget.CompiledText.NumericalVariable,
    var linesColour: Colour = Colour.WHITE,
    var backgroundColour: Colour = Colour(0.35f, 0f, 0f, 0f),
    // Whether or not the bottom of the graph should be at zero, or at the minimum sample.
    var baseLineZero: Boolean = true
) : PinnableWidget(name, position, open, pinned, background) {

    val numVarMap by lazy {
        VarMap.inner.mapNotNull {
            // This is an unfortunate way to check if this VarMap entry is one for a numerical variable, but I cba to refactor varmap for this small thing
            if (it.value() is TextPinnableWidget.CompiledText.NumericalVariable) {
                it.key to it.value // Turning Map.Entry into a Pair, so we can call `toMap`
            } else {
                null
            }
        }.toMap()
    }

    val numVarMapComboItems by lazy {
        numVarMap.keys.joinToString("$NUL") { it.toLowerCase().capitalize() }
    }

    var edit = false
    private var editVarComboIndex = -1

    // Only used in UI. Represents how many seconds of data the capacity represents.
    var seconds = capacity / sampleRate
    var samples = ArrayList<Float>(capacity)
    private var refreshTime = 0.0

    init {
        // The user can resize graph widgets as they wish
        this.autoResize = false
    }

    override fun fillWindow() {
        if (sampleRate != 0f) {
            if (refreshTime == 0.0) refreshTime = time
            while (refreshTime < time) {
                this.variable.provideNumber()?.let {
                    samples.add(0, it.toFloat())
                    if (samples.size > capacity)
                        samples.removeLast()
                    refreshTime += 1 / sampleRate
                }
            }
        }
        dsl.withStyleColor(Col.PlotLines, linesColour.asVec4()) {
            dsl.withStyleColor(Col.FrameBg, backgroundColour.asVec4()) {
                dsl.withItemWidth(-(ImGui.calcTextSize(variable.name).x + ImGui.style.windowPadding.x)) {
                    ImGui.plotLines(
                        variable.name,
                        { idx -> samples[idx] },
                        samples.size,
                        scaleMin = baseLineZero.to(0f, Float.MAX_VALUE),
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
        if (edit && mc.currentScreen is KamiGuiScreen) {
            dsl.window("Edit $name", ::edit, WindowFlag.AlwaysAutoResize.i) {
                editVarComboIndex = numVarMap.keys.indexOf(this.variable.name)
                dsl.combo("Variable##${name}-graph-var", ::editVarComboIndex, numVarMapComboItems) {
                    numVarMap[numVarMap.keys.toList()[editVarComboIndex]]?.let { it() }?.let {
                        this.variable = it as TextPinnableWidget.CompiledText.NumericalVariable
                        this.samples.clear()
                    }
                }

                // We store in variables instead of doing it in the if statement to prevent the conditional from short-circuiting, and not displaying a slider for one frame.
                val sampleChanged = ImGui.dragFloat(
                    "Sample rate##${name}-graph-sr",
                    ::sampleRate,
                    vMin = 0f,
                    vMax = 60f,
                    vSpeed = 0.05f,
                    format = "%.2f Hz"
                )
                val secondsChanged = ImGui.dragFloat(
                    "Capacity##${name}-graph-capacity-seconds",
                    ::seconds,
                    vMin = 0f,
                    vMax = 60f * 60f,
                    vSpeed = 0.05f,
                    format = "%.0f seconds"
                )
                if (sampleChanged || secondsChanged) {
                    this.capacity = (seconds * sampleRate).roundToInt()
                    // In case the capacity shrunk, remove those elements
                    while (samples.size > capacity)
                        samples.removeLast()
                }
                ImGui.sameLine()
                ImGui.textDisabled("= $capacity samples")

                ImGui.checkbox("Base at zero", ::baseLineZero)

                KamiConfig.colourType.settingInterface?.let { interf ->
                    interf.displayImGui("Line colour", linesColour)?.let { linesColour = it }
                    interf.displayImGui("Background colour", backgroundColour)?.let { backgroundColour = it }
                }
            }
        }
    }
}
