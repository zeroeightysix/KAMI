package me.zeroeightsix.kami.setting

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.SettingNamingConvention

object ProperCaseConvention : SettingNamingConvention {
    override fun name(name: String?): String = name!!.replace("_|-", " ").capitalize()
}
