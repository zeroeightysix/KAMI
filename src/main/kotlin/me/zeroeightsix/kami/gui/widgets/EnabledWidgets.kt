package me.zeroeightsix.kami.gui.widgets

import com.google.common.collect.Iterables
import imgui.ImGui.separator
import imgui.dsl.checkbox
import imgui.dsl.menu
import imgui.dsl.menuItem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.ConfigSaveEvent
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.feature.FindSettings

@FindFeature
@FindSettings(settingsRoot = "clickGui")
object EnabledWidgets : Feature, Listenable {

    @Setting
    var hideAll = false

    override var name: String = "EnabledWidgets"
    override var hidden: Boolean = true

    @Setting(name = "Widgets")
    internal var textWidgets = arrayListOf(
        Information,
        Coordinates,
        ActiveModules
    )

    @Setting(name = "PlayerOverlays")
    internal var playerWidgets = arrayListOf<PlayerPinnableWidget>()

    @Setting(name = "InventoryOverlays")
    internal var inventoryWidgets = arrayListOf<InventoryPinnableWidget>()

    val widgets
        get() = Iterables.concat(textWidgets, playerWidgets, inventoryWidgets) as MutableIterable

    operator fun invoke() = menu("Overlay") {
        checkbox("Hide all", EnabledWidgets::hideAll) {}
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

    @EventHandler
    val saveListener = Listener<ConfigSaveEvent>({
        // Changes the instance of widgets, invalidating the fiber serialisation cache, forcing fiber to re-serialise widgets.
        textWidgets = ArrayList(textWidgets)
    })

    override fun initListening() {
        KamiMod.EVENT_BUS.subscribe(saveListener)
    }

}
