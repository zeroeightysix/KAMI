package me.zeroeightsix.kami.feature

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Listener
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listenable
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.BindEvent
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.then
import me.zeroeightsix.kami.util.Bind
import me.zero.alpine.listener.Listener as AlpineListener

open class FullFeature(
    override var name: String = "No name",
    var description: String = "No description",
    _alwaysListening: Boolean = false,
    override var hidden: Boolean = false
) : Feature, Listenable, HasConfig, HasBind {

    override lateinit var config: ConfigBranch

    var alwaysListening = _alwaysListening
        set(value) {
            field = value
            if (value) KamiMod.EVENT_BUS.subscribe(this)
            else if (!value && !enabled) KamiMod.EVENT_BUS.unsubscribe(this)
        }

    @EventHandler
    val bindListener = AlpineListener(
        EventHook<BindEvent> {
            // If this is a repeat event (they key was held down long enough for it to start repeating), ignore it.
            if (it.i == 2) return@EventHook
            if (bind.update(it.key, it.scancode, it.pressed) && it.pressed && it.ingame) {
                this.enabled = !this.enabled
            }
        }
    )

    init {
        KamiMod.EVENT_BUS.subscribe(bindListener)
    }

    @Setting(name = "Bind")
    @SettingVisibility.Constant(false)
    override var bind = Bind.none()

    @Setting
    @SettingVisibility.Constant(false)
    var displayName: @Setting.Constrain.MinLength(1) String = name

    @Setting
    @SettingVisibility.Constant(false)
    var enabled = false
        set(value) {
            field = value.also {
                if (it && !field) {
                    handleEnabled()
                } else if (!it && field) {
                    handleDisabled()
                }
            }
        }

    // do not listen to intellij
    // it lies
    @Listener("Enabled")
    private fun enabledChanged(old: java.lang.Boolean, new: java.lang.Boolean) {
        if (old != null && new != null && !old.equals(new)) {
            if (new.booleanValue()) {
                handleEnabled()
            } else {
                handleDisabled()
            }
        }
    }

    fun enable(): Boolean {
        return (!enabled).then {
            enabled = true
            true
        } ?: false
    }

    fun disable(): Boolean {
        return enabled.then {
            enabled = false
            true
        } ?: false
    }

    private fun handleEnabled() {
        if (!alwaysListening) {
            KamiMod.EVENT_BUS.subscribe(this)
        }
        onEnable()
    }

    private fun handleDisabled() {
        if (!alwaysListening) {
            KamiMod.EVENT_BUS.unsubscribe(this)
        }
        onDisable()
    }

    open fun onEnable() {}
    open fun onDisable() {}
}
