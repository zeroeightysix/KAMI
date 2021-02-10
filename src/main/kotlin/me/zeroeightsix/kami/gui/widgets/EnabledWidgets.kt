package me.zeroeightsix.kami.gui.widgets

import com.google.common.collect.Iterables
import imgui.ImGui.separator
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.Listenable
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.gui.ImguiDSL.checkbox
import me.zeroeightsix.kami.gui.ImguiDSL.menu
import me.zeroeightsix.kami.gui.ImguiDSL.menuItem
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.setting.guiService

@FindFeature
object EnabledWidgets : Feature, Listenable {

    @Setting
    var hideAll: Boolean = false

    override var name: String = "EnabledWidgets"

    override var hidden: Boolean = true
    @Setting(name = "Widgets")
    internal var textWidgets = mutableListOf(
        Information,
        Coordinates,
        ActiveModules
    )

    @Setting(name = "PlayerOverlays")
    internal var playerWidgets = arrayListOf<PlayerPinnableWidget>()

    @Setting(name = "InventoryOverlays")
    internal var inventoryWidgets = arrayListOf<InventoryPinnableWidget>()

    @Setting(name = "Graphs")
    internal var graphs = arrayListOf<GraphPinnableWidget>()

    val widgets
        get() = Iterables.concat(textWidgets, playerWidgets, inventoryWidgets, graphs) as MutableIterable

    override fun init() {
        KamiConfig.register(guiService("widgets"), this)
    }

    operator fun invoke() = menu("Overlay") {
        checkbox("Hide all", ::hideAll)
        separator()
        enabledButtons()
        separator()
        menuItem("Pin all") {
            widgets.forEach {
                it.pinned = true
            }
        }
        menuItem("Unpin all") {
            widgets.forEach {
                it.pinned = false
            }
        }
    }

    fun enabledButtons() {
        widgets.forEach { widget ->
            menuItem(widget.name, "", widget.open, !hideAll) {
                widget.open = !widget.open
            }
        }
    }
}