package me.zeroeightsix.kami.gui.windows.modules

import me.zeroeightsix.kami.module.Module

data class ModulePayload(val set: MutableSet<Module>, val source: Modules.ModuleWindow)

object Payloads {

    var payload: ModulePayload? = null

    const val KAMI_MODULE_PAYLOAD = "KAMI_MODS"

    fun needsPayload() = payload == null

}