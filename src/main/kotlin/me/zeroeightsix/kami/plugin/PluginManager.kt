package me.zeroeightsix.kami.plugin

object PluginManager {

    val plugins: MutableList<Plugin> = mutableListOf()

    fun getPlugin(name: String) = plugins.find { it.name.value == name }

    fun registerPlugin(plugin: Plugin) {
        plugins.add(plugin)
    }

    fun enablePlugin(name: String) = getPlugin(name)?.enable()
    fun disablePlugin(name: String) = getPlugin(name)?.disable()

}