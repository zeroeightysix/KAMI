package me.zeroeightsix.kami.feature

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Listener
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.then
import me.zeroeightsix.kami.util.Bind

open class FullFeature(
    var originalName: String = "No name",
    var description: String = "No description",
    hidden: Boolean = false,
    _alwaysListening: Boolean = false
) : AbstractFeature(hidden),
    Listening {

    lateinit var config: ConfigBranch

    var alwaysListening = _alwaysListening
        set(value) {
            field = value
            if (value) KamiMod.EVENT_BUS.subscribe(this)
            else if (!value && isDisabled()) KamiMod.EVENT_BUS.unsubscribe(this)
        }

    @Setting(name = "Bind")
    private var bind = Bind.none()

    @Setting
    @SettingVisibility.Constant(false)
    var name: @Setting.Constrain.MinLength(1) String = originalName

    @Setting
    @SettingVisibility.Constant(false)
    var enabled = false

    // do not listen to intellij
    // it lies
    @Listener("Enabled")
    private fun enabledChanged(old: java.lang.Boolean, new: java.lang.Boolean) {
        if (old != null && new != null && !old.equals(new)) {
            if (new.booleanValue()) onEnable()
            else onDisable()
        }
    }

    override fun enable(): Boolean {
        return isDisabled().then {
            enabled = true
            true
        } ?: false
    }

    override fun disable(): Boolean {
        return isEnabled().then {
            enabled = false
            true
        } ?: false
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun isDisabled(): Boolean {
        return !isEnabled()
    }

    override fun onEnable() {
        if (!alwaysListening) KamiMod.EVENT_BUS.subscribe(this)
    }

    override fun onDisable() {
        if (!alwaysListening) KamiMod.EVENT_BUS.unsubscribe(this)
    }

    override fun isAlwaysListening(): Boolean = alwaysListening

    override fun getBind(): Bind = bind

}
