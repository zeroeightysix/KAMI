package me.zeroeightsix.kami.gui.windows.modules

import me.zeroeightsix.kami.module.Module

internal data class ModulePayload(val modules: MutableSet<Module>, val source: Modules.ModuleWindow, val groupName: String? = null) {
    fun moveTo(target: Modules.ModuleWindow, targetGroup: String) {
        // Start by removing the module(s) from the payload's source
        val newSourceGroups = mutableMapOf<String, MutableList<Module>>()
        for ((group, list) in source.groups) {
            val newList = list.filter { !this.modules.contains(it) }.toMutableList()
            if (newList.isNotEmpty()) {
                newSourceGroups[group] = newList
            }
        }
        source.groups = newSourceGroups

        // Add the modules to target window
        val newTargetGroups = target.groups.toMutableMap()
        val list = (newTargetGroups[targetGroup] ?: mutableListOf()).toMutableList()
        list.addAll(modules)
        newTargetGroups[targetGroup] = list
        target.groups = newTargetGroups
    }

    /**
     * Generate a window title based on this payload's contents
     */
    fun inventName(): String {
        if (groupName != null)
            return groupName
        return when (modules.size) {
            1 -> modules.find { true }!!.name
            else -> "${modules.size} modules"
        }
    }
}

internal object Payloads {

    const val KAMI_MODULE_PAYLOAD = "KAMI_MODS"

}