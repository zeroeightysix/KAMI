package me.zeroeightsix.kami

import baritone.api.BaritoneAPI
import glm_.vec2.Vec2
import imgui.ImGui
import imgui.WindowFlag
import imgui.cStr
import imgui.dsl
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.setting.settingInterface

object BaritoneIntegration {

    private val present by lazy {
        try {
            Class.forName("baritone.Baritone")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    val recentControlProcess
        get() = BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl().kotlin

    private val baritoneSettings by lazy {
        BaritoneAPI.getSettings().allSettings.mapNotNull { setting ->
            val name = setting.name
            val type = setting.type as? Class<*> ?: return@mapNotNull null

            @Suppress("UNCHECKED_CAST")
            val configType = when {
                // Two settings (one int and one long) have a default type of -1, so we allow -1 as a special value on all int and long settings.
                type === java.lang.Integer::class.java -> ConfigTypes.INTEGER.withMinimum(-1)
                type === java.lang.Long::class.java -> ConfigTypes.LONG.withMinimum(-1L)
                type === java.lang.Double::class.java -> ConfigTypes.DOUBLE.withMinimum(0.0)
                type === java.lang.Float::class.java -> ConfigTypes.FLOAT.withMinimum(0f)
                type === java.lang.Boolean::class.java -> ConfigTypes.BOOLEAN
                type === java.lang.String::class.java -> ConfigTypes.STRING
                else -> return@mapNotNull null
            } as ConfigType<Any, out Any, *>
            KamiConfig.installBaseExtension(configType)

            name to {
                dsl.withItemWidth(120) {
                    configType.settingInterface?.displayImGui(name, setting.value)?.let {
                        setting.value = it
                    }
                }
            }
        }.toMutableList()
    }

    private var settingsOpen = false
    private var filter = ByteArray(128)

    fun menu() {
        this {
            dsl.menu("Baritone") {
                dsl.menuItem("Settings", selected = settingsOpen) {
                    settingsOpen = !settingsOpen
                }
            }

            if (settingsOpen) {
                dsl.window("Baritone settings", ::settingsOpen, flags = WindowFlag.AlwaysAutoResize.i) {
                    var resetScroll = false
                    if (ImGui.inputText("Filter##baritone-settings-filter", filter)) {
                        // If the user searched something, reset their scroll to the top left.
                        // This way the most relevant search result will always be visible.
                        resetScroll = true
                        val filter = filter.cStr
                        if (filter.isNotEmpty()) {
                            baritoneSettings.sortByDescending {
                                FuzzySearch.partialRatio(filter.toLowerCase(), it.first.toLowerCase())
                            }
                        } else {
                            baritoneSettings.sortBy { it.first }
                        }
                    }
                    dsl.child(
                        "baritone-settings-settings",
                        size = Vec2(0, 450),
                        extraFlags = WindowFlag.AlwaysHorizontalScrollbar.i
                    ) {
                        if (resetScroll) {
                            ImGui.currentWindow.scroll = Vec2(0, 0)
                        }
                        baritoneSettings.forEach { (_, displayer) ->
                            displayer()
                        }
                    }
                }
            }
        }
    }

    /**
     * Run `block` if baritone integration is present
     */
    operator fun invoke(block: () -> Unit) {
        if (present) block()
    }
}
