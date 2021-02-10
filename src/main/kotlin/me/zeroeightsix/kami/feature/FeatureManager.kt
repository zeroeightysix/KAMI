package me.zeroeightsix.kami.feature

import java.util.Collections
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.feature.command.Command
import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.feature.plugin.Plugin
import org.reflections.Reflections

object FeatureManager {

    val features = mutableListOf<Feature>()

    val modules get() = features.filterIsInstance<Module>()
    val plugins get() = features.filterIsInstance<Plugin>()
    val fullFeatures get() = features.filterIsInstance<FullFeature>()

    init {
        initFeatures()
    }

    fun <T : FullFeature> List<T>.getByName(name: String): T? {
        return this.firstOrNull { it.name == name }
    }

    fun addFeature(feature: Feature): Boolean {
        return features.add(feature)
    }

    fun removeFeature(feature: Feature): Boolean {
        return features.remove(feature)
    }

    /**
     * Resolves all features annotated by [FindFeature] or affected by the annotation on a superclass.
     *
     * @see KamiConfig.findAnnotatedSettings
     */
    fun findAnnotatedFeatures(): Set<Class<out Any>> {
        val reflections = Reflections("me.zeroeightsix.kami")
        return reflections.getTypesAnnotatedWith(FindFeature::class.java).flatMap {
            val ffAnnot = it.getAnnotation(FindFeature::class.java)
            ffAnnot?.findDescendants?.let { des ->
                if (des) {
                    reflections.getSubTypesOf(it)
                } else {
                    null
                }
            } ?: Collections.singleton(it)
        }.toSet() // to remove duplicates
    }

    private fun initFeatures() {
        findAnnotatedFeatures().forEach {
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
            tryErr(
                {
                    val feature = it.getConstructor().newInstance() as Feature
                    features.add(feature)
                },
                {
                    val instFeature = it.getDeclaredField("INSTANCE").get(null) as Feature
                    features.add(instFeature)
                }
            )
        }

        features.forEach {
            it.init()
        }

        features.filterIsInstance<Command>().forEach {
            it.register(Command.dispatcher)
        }

        features.sortWith(
            compareBy {
                if (it is FullFeature) it.name else null
            }
        )

        KamiMod.log.info("Features initialised")
    }
}