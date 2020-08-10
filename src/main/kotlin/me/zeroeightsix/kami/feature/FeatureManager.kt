package me.zeroeightsix.kami.feature

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.feature.command.Command
import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.feature.plugin.Plugin
import me.zeroeightsix.kami.util.Wrapper
import org.reflections.Reflections
import java.util.stream.Stream

/**
 * Created by 086 on 23/08/2017.
 */
object FeatureManager {

    val features = mutableListOf<AbstractFeature>()

    val modules get() = features.filterIsInstance<Module>()
    val plugins get() = features.filterIsInstance<Plugin>()
    val fullFeatures get() = features.filterIsInstance<FullFeature>()

    init {
        initFeatures()
    }

    fun <T : FullFeature> List<T>.getByName(name: String): T? {
        return this.firstOrNull { it.name == name }
    }

    @JvmStatic
    fun onBind(key: Int, scancode: Int, i: Int) {
        val pressed = i != 0
        if (Wrapper.getMinecraft().currentScreen != null && pressed) {
            return
        }
        features.filter { it is Listening }.forEach {
            val l = it as Listening
            val bind = l.getBind()
            if ((bind.code.keysym && bind.code.key == key) || (!bind.code.keysym && bind.code.scan == scancode))
                bind.pressed = pressed

            if (bind.isDown) {
                it.toggle()
            }
        }
    }

    fun addFeature(feature: AbstractFeature): Boolean {
        return features.add(feature)
    }

    fun removeFeature(feature: AbstractFeature): Boolean {
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
            tryErr({
                val feature = it.getConstructor().newInstance() as AbstractFeature
                features.add(feature)
            }, {
                val instFeature = it.getDeclaredField("INSTANCE").get(null) as AbstractFeature
                features.add(instFeature)
            })
        }

        modules.forEach {
            it.initListening()
        }

        features.filterIsInstance<Command>().forEach {
            it.register(Command.dispatcher)
        }

        features.sortWith(compareBy {
            if (it is FullFeature) it.name else null
        })

        // All 'always listening' features are now registered to the event bus, never to be unregistered.
        features.filterIsInstance<Listening>()
            .filter { it.isAlwaysListening() }
            .forEach {
                KamiMod.EVENT_BUS.subscribe(
                    it
                )
            }

        KamiMod.log.info("Features initialised")
    }

}
