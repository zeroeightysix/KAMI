package me.zeroeightsix.kami.feature

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.mixin.client.IKeyBinding
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.util.InputUtil
import org.reflections.Reflections
import java.util.stream.Stream

/**
 * Created by 086 on 23/08/2017.
 */
object FeatureManager {

    val features = mutableListOf<Feature>()
    /**
     * Lookup map for getting by name
     */
    var lookup = mutableMapOf<String, Feature>()
    
    fun initialize() {
        initFeatures()
    }

    @JvmStatic
    fun updateLookup() {
        lookup.clear()
        for (m in features) lookup[m.name.value.toLowerCase()] = m
    }

    @JvmStatic
    fun onBind(key: Int, scancode: Int, i: Int) {
        val pressed = i != 0
        val code = InputUtil.getKeyCode(key, scancode)
        if (Wrapper.getMinecraft().currentScreen != null) {
            return
        }
        if (key == 89 && scancode == 29) {
            if (KamiMod.getInstance().kamiGuiScreen == null) {
                KamiMod.getInstance().kamiGuiScreen = KamiGuiScreen()
            }
            Wrapper.getMinecraft().openScreen(KamiMod.getInstance().kamiGuiScreen)
        }
        features.filter { it is Listening }.forEach {
            val l = it as Listening
            val bind = l.getBind()
            if ((bind.binding as IKeyBinding).keyCode == code) {
                (bind.binding as IKeyBinding).setPressed(pressed)
            }
            if (bind.isDown) {
                it.toggle()
            }
        }
    }

    @JvmStatic
    fun getModuleByName(name: String): Module? {
        return lookup[name.toLowerCase()] as Module
    }

    fun addFeature(feature: Feature): Boolean {
        return features.add(feature)
    }

    fun removeFeature(feature: Feature): Boolean {
        return features.remove(feature)
    }

    @JvmStatic
    fun isModuleEnabled(moduleName: String): Boolean {
        val m = getModuleByName(moduleName) ?: return false
        return m.isEnabled()
    }

    private fun initFeatures() {
        val reflections = Reflections()
        Stream.concat(
            reflections.getSubTypesOf(Module::class.java).stream(),
            reflections.getTypesAnnotatedWith(FindFeature::class.java).stream()
        ).forEach {
            fun tryErr(block: () -> Unit) {
                try {
                    block()
                } catch (e: Exception) {
                    e.printStackTrace()
                    System.err.println("Couldn't initiate feature " + it.simpleName + "! Err: " + e.javaClass.simpleName + ", message: " + e.message)
                }
            }
            tryErr {
                val feature = it.getConstructor().newInstance() as Feature
                features.add(feature)
                tryErr {
                    val instFeature = it.getDeclaredField("INSTANCE").get(null) as Feature
                    features.add(instFeature)
                }
            }
        }

        KamiMod.log.info("Features initialised")
        features.sortWith(compareBy { it.name.value })
    }

}