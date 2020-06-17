package me.zeroeightsix.kami.feature

import com.google.common.base.Converter
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import glm_.vec2.Vec2
import imgui.ImGui.button
import imgui.ImGui.sameLine
import imgui.ImGui.text
import me.zeroeightsix.fiber.api.annotation.Listener
import me.zeroeightsix.fiber.api.annotation.Setting
import me.zeroeightsix.fiber.api.annotation.Settings
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.mixin.extend.getKeyCode
import me.zeroeightsix.kami.setting.SettingDisplay
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.then
import me.zeroeightsix.kami.to
import me.zeroeightsix.kami.util.Bind
import net.minecraft.client.util.InputUtil

@Settings(onlyAnnotated = true)
open class FullFeature(
    protected var originalName: String = "No name",
    var description: String = "No description",
    hidden: Boolean = false,
    _alwaysListening: Boolean = false
) : AbstractFeature(hidden),
    Listening {

    var alwaysListening = _alwaysListening
        set(value) {
            field = value
            if (value) KamiMod.EVENT_BUS.subscribe(this)
            else if (!value && isDisabled()) KamiMod.EVENT_BUS.unsubscribe(this)
        }

    @SettingDisplay("showBind")
    @Setting(name = "Bind")
    private var bind = Bind.none()
    
    private fun showBind(bind: Bind) {
        text("Bound to $bind") // TODO: Highlight bind in another color?
        sameLine(0, -1)
        if (button("Bind", Vec2())) { // TODO: Bind popup?
            // Maybe just display "Press a key" instead of the normal "Bound to ...", and wait for a key press.
        }
    }

    @Setting
    @SettingVisibility(false)
    var name: @Setting.Constrain.MinLength(1) String = originalName

    @Setting
    @SettingVisibility(false)
    @Listener("enabledChanged")
    var enabled: Boolean = false
    
    private fun enabledChanged(old: Boolean, new: Boolean) {
        if (old != new) {
            if (new) onEnable()
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

    private class BindConverter : Converter<Bind, JsonElement>() {
        override fun doForward(bind: Bind): JsonElement {
            val array = JsonArray()
            array.add(bind.isAlt)
            array.add(bind.isCtrl)
            array.add(bind.isShift)
            array.add(bind.binding.getKeyCode().category == InputUtil.Type.KEYSYM)
            array.add(bind.binding.getKeyCode().keyCode)
            return array
        }

        override fun doBackward(jsonElement: JsonElement): Bind {
            val array = jsonElement.asJsonArray
            val alt = array[0].asBoolean
            val ctrl = array[1].asBoolean
            val shift = array[2].asBoolean
            val keysm = array[3].asBoolean
            val code = array[4].asInt
            return Bind(ctrl, alt, shift, InputUtil.getKeyCode(keysm.to(code, -1), keysm.to(-1, code)))
        }
    }

}