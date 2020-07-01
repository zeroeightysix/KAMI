package me.zeroeightsix.kami;

import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.zeroeightsix.kami.feature.AbstractFeature;
import me.zeroeightsix.kami.feature.FeatureManager;
import me.zeroeightsix.kami.feature.Listening;
import me.zeroeightsix.kami.setting.KamiConfig;
import me.zeroeightsix.kami.util.LagCompensator;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by 086 on 7/11/2017.
 */
public class KamiMod implements ModInitializer {

    public static final String MODNAME = "KAMI";
    public static final String MODVER = "fabric-1.14.4-debug";
    public static final String KAMI_KANJI = "\u795E";

    public static final Logger log = LogManager.getLogger("KAMI");
    public static final EventBus EVENT_BUS = new EventManager();
    public static int rainbow = 0xFFFFFF; // This'll be updated every tick
    private static KamiMod INSTANCE;
    
    private ConfigTree config;

    @Override
    public void onInitialize() {
        KamiMod.INSTANCE = this;
        EVENT_BUS.subscribe(KamiMod.INSTANCE);

        KamiMod.log.info("Initialising " + MODNAME + " " + MODVER);

        FeatureManager manager = FeatureManager.INSTANCE;
        manager.initialize();

        FeatureManager.INSTANCE.getFeatures()
                .stream()
                .filter(feature -> feature instanceof Listening && ((Listening) feature).isAlwaysListening())
                .forEach(EVENT_BUS::subscribe);
        LagCompensator.INSTANCE = new LagCompensator();

        this.config = KamiConfig.INSTANCE.initAndLoad();

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        FeatureManager.INSTANCE.getFeatures().stream().filter(AbstractFeature::isEnabled).forEach(AbstractFeature::enable);

        KamiMod.log.info(MODNAME + " initialised");
    }

    public ConfigTree getConfig() {
        return config;
    }

    public static KamiMod getInstance() {
        return INSTANCE;
    }

}
