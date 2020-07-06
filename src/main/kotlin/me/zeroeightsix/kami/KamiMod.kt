package me.zeroeightsix.kami

import me.zero.alpine.bus.EventBus
import me.zero.alpine.bus.EventManager
import me.zeroeightsix.kami.feature.AbstractFeature
import me.zeroeightsix.kami.feature.FeatureManager
import me.zeroeightsix.kami.feature.FeatureManager.features
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.util.LagCompensator
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager

/**
 * Created by 086 on 7/11/2017.
 */
// We use a class instead of an object so we don't need to use the kotlin language adapter just to initialise KAMI
class KamiMod : ModInitializer {
    companion object {
        const val MODNAME = "KAMI"
        const val MODVER = "fabric-1.14.4-debug"
        const val KAMI_KANJI = "\u795E"

        val log = LogManager.getLogger("KAMI")
        @JvmField
        val EVENT_BUS: EventBus = EventManager()
        var rainbow = 0xFFFFFF // This'll be updated every tick
    }

    override fun onInitialize() {
        log.info("Initialising $MODNAME $MODVER")

        FeatureManager // Initialises FeatureManager, which finds & initialises ALL features
        LagCompensator.INSTANCE = LagCompensator()
        KamiConfig // Initialises KamiConfig, which constructs & loads config

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        features.stream()
            .filter { obj: AbstractFeature -> obj.isEnabled() }.forEach { obj: AbstractFeature -> obj.enable() }

        log.info("$MODNAME initialised")
    }
}
