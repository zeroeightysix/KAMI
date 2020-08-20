package me.zeroeightsix.kami

import me.zero.alpine.bus.EventBus
import me.zero.alpine.bus.EventManager
import me.zeroeightsix.kami.feature.FeatureManager
import me.zeroeightsix.kami.setting.KamiConfig
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager

// We use a class instead of an object so we don't need to use the kotlin language adapter just to initialise KAMI
class KamiMod : ModInitializer {
    companion object {
        const val MODNAME = "KAMI"
        const val MODVER = "fabric-1.16.2-debug"
        const val KAMI_KANJI = "\u795E"

        @JvmStatic
        val log = LogManager.getLogger("KAMI")
        @JvmField
        val EVENT_BUS: EventBus = EventManager()
        var rainbow = 0xFFFFFF // This'll be updated every tick
    }

    override fun onInitialize() {
        log.info("Initialising $MODNAME $MODVER")

        FeatureManager // Initialises FeatureManager, which finds & initialises ALL features
        KamiConfig // Initialises KamiConfig, which constructs & loads config

        log.info("$MODNAME initialised")
    }
}
