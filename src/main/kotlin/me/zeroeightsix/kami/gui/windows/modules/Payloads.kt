package me.zeroeightsix.kami.gui.windows.modules

import me.zeroeightsix.kami.module.Module

data class ModulePayload(val set: MutableSet<Module>, val source: Modules.ModuleWindow)

const val KAMI_MODULE_PAYLOAD = "KAMI_MODS"

object Payloads {

    var payload: ModulePayload? = null

    fun needsPayload() = payload == null

}