package me.zeroeightsix.kami.feature

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.feature.command.Command
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.mixin.client.IKeyBinding
import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.util.InputUtil
import org.reflections.Reflections
import java.util.stream.Stream

/**
 * Created by 086 on 23/08/2017.
 */
object FeatureManager {

    val features = mutableListOf<Feature>()

    fun initialize() {
        initFeatures()
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

    fun addFeature(feature: Feature): Boolean {
        return features.add(feature)
    }

    fun removeFeature(feature: Feature): Boolean {
        return features.remove(feature)
    }

    private fun initFeatures() {
        val reflections = Reflections()
        Stream.concat(
            Stream.concat(
                reflections.getSubTypesOf(Module::class.java).stream(),
                reflections.getSubTypesOf(Command::class.java).stream()
            ),
            reflections.getTypesAnnotatedWith(FindFeature::class.java).stream()
        ).forEach {
            fun tryErr(block: () -> Unit, or: () -> Unit) {
                try {
                    block()
                } catch (e: Exception) {
                    try {
                        or()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        System.err.println("Couldn't initiate feature " + it.simpleName + "! Err: " + e.javaClass.simpleName + ", message: " + e.message)
                    }
                }
            }
            tryErr( {
                val feature = it.getConstructor().newInstance() as Feature
                features.add(feature)
            }, {
                val instFeature = it.getDeclaredField("INSTANCE").get(null) as Feature
                features.add(instFeature)
            })
        }

        KamiMod.log.info("Features initialised")

        features.filterIsInstance<Command>().forEach {
            it.register(Command.dispatcher)
        }

        features.sortWith(compareBy { it.name.value })
    }

}