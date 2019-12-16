package me.zeroeightsix.kami;

import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.CommandManager;
import me.zeroeightsix.kami.event.events.DisplaySizeChangedEvent;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.setting.config.Configuration;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
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

    private int displayWidth;
    private int displayHeight;

    public CommandManager commandManager;

    @EventHandler
    private Listener<TickEvent.Client> clientTickListener = new Listener<>(event -> {
        if (MinecraftClient.getInstance().window.getWidth() != displayWidth || MinecraftClient.getInstance().window.getHeight() != displayHeight) {
            KamiMod.EVENT_BUS.post(new DisplaySizeChangedEvent());
            displayWidth = MinecraftClient.getInstance().window.getWidth();
            displayHeight = MinecraftClient.getInstance().window.getHeight();

            // TODO: new GUI
//            KamiMod.getInstance().getGuiManager().getChildren().stream()
//                    .filter(component -> component instanceof Frame)
//                    .forEach(component -> KamiGUI.dock((Frame) component));
        }
    });

    @Override
    public void onInitialize() {
        KamiMod.INSTANCE = this;

        KamiMod.log.info("\n\nInitializing KAMI " + MODVER);

        ModuleManager manager = ModuleManager.initialize();
        EVENT_BUS.subscribe(manager);

        ModuleManager.getModules().stream().filter(module -> module.alwaysListening).forEach(EVENT_BUS::subscribe);
//        MinecraftForge.EVENT_BUS.register(new ForgeEventProcessor());
        LagCompensator.INSTANCE = new LagCompensator();

        // TODO: New GUI
//        guiManager = new KamiGUI();
//        guiManager.initializeGUI();

        commandManager = new CommandManager();

        Friends.initFriends();
        SettingsRegister.register("commandPrefix", Command.commandPrefix);
        loadConfiguration();
        KamiMod.log.info("Settings loaded");

        ModuleManager.updateLookup(); // generate the lookup table after settings are loaded to make custom module names work

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        ModuleManager.getModules().stream().filter(Module::isEnabled).forEach(Module::enable);

        KamiMod.log.info("KAMI Mod initialized!\n");
    }

    public static String getConfigName() {
        Path config = Paths.get("KAMILastConfig.txt");
        String kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
        try(BufferedReader reader = Files.newBufferedReader(config)) {
            kamiConfigName = reader.readLine();
            if (!isFilenameValid(kamiConfigName)) kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
        } catch (NoSuchFileException e) {
            try(BufferedWriter writer = Files.newBufferedWriter(config)) {
                writer.write(KAMI_CONFIG_NAME_DEFAULT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kamiConfigName;
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
        ModuleManager.getModules().forEach(Module::destroy);
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

    public CommandManager getCommandManager() {
        return commandManager;
    }

}
