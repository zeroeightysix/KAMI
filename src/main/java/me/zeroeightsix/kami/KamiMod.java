package me.zeroeightsix.kami;

import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.zeroeightsix.kami.feature.command.Command;
import me.zeroeightsix.kami.feature.Feature;
import me.zeroeightsix.kami.feature.FeatureManager;
import me.zeroeightsix.kami.feature.Listening;
import me.zeroeightsix.kami.gui.KamiGuiScreen;
import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.setting.config.Configuration;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by 086 on 7/11/2017.
 */
public class KamiMod implements ModInitializer {

    public static final String MODID = "kami";
    public static final String MODNAME = "KAMI";
    public static final String MODVER = "b9";
    public static final String KAMI_KANJI = "\u795E";
    private static final String KAMI_CONFIG_NAME_DEFAULT = "KAMIConfig.json";

    public static final Logger log = LogManager.getLogger("KAMI");
    public static final EventBus EVENT_BUS = new EventManager();
    private static KamiMod INSTANCE;

    public KamiGuiScreen kamiGuiScreen = null;
    public static int rainbow = 0xFFFFFF; // This'll be updated every tick

    @Override
    public void onInitialize() {
        KamiMod.INSTANCE = this;
        EVENT_BUS.subscribe(KamiMod.INSTANCE);

        KamiMod.log.info("\n\nInitializing KAMI " + MODVER);

        FeatureManager manager = FeatureManager.INSTANCE;
        manager.initialize();

        FeatureManager.INSTANCE.getFeatures()
                .stream()
                .filter(feature -> feature instanceof Listening && ((Listening) feature).isAlwaysListening())
                .forEach(EVENT_BUS::subscribe);
        LagCompensator.INSTANCE = new LagCompensator();

        Friends.initFriends();
        SettingsRegister.register("commandPrefix", Command.commandPrefix);
        loadConfiguration();
        KamiMod.log.info("Settings loaded");

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        FeatureManager.INSTANCE.getFeatures().stream().filter(Feature::isEnabled).forEach(Feature::enable);

        KamiMod.log.info("KAMI Mod initialized!\n");
    }

    public static String getConfigName() {
        Path config = Paths.get("KAMILastConfig.txt");
        String kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
        try(BufferedReader reader = Files.newBufferedReader(config)) {
            kamiConfigName = reader.readLine();
            if (!isFilenameValid(kamiConfigName)) kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
        } catch (NoSuchFileException e) {
            setLastConfigName(KAMI_CONFIG_NAME_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kamiConfigName;
    }

    public static void setLastConfigName(String newConfigName) {
        Path config = Paths.get("KAMILastConfig.txt");
        try(BufferedWriter writer = Files.newBufferedWriter(config)) {
            writer.write(newConfigName);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void loadConfiguration() {
        try {
            loadConfigurationUnsafe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadConfigurationUnsafe() throws IOException {
        String kamiConfigName = getConfigName();
        Path kamiConfig = Paths.get(kamiConfigName);
        if (!Files.exists(kamiConfig)) return;
        Configuration.loadConfiguration(kamiConfig);
    }

    public static void saveConfiguration() {
        try {
            saveConfigurationUnsafe();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfigurationUnsafe() throws IOException {
        Path outputFile = Paths.get(getConfigName());
        if (!Files.exists(outputFile))
            Files.createFile(outputFile);
        Configuration.saveConfiguration(outputFile);
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static KamiMod getInstance() {
        return INSTANCE;
    }

}
