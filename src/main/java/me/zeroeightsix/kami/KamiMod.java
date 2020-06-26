package me.zeroeightsix.kami;

import com.mojang.authlib.GameProfile;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ListConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.MapConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.zeroeightsix.kami.feature.AbstractFeature;
import me.zeroeightsix.kami.feature.FeatureManager;
import me.zeroeightsix.kami.feature.Listening;
import me.zeroeightsix.kami.mixin.client.IKeyBinding;
import me.zeroeightsix.kami.setting.ProperCaseConvention;
import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 086 on 7/11/2017.
 */
public class KamiMod implements ModInitializer {

    public static final String MODNAME = "KAMI";
    public static final String MODVER = "fabric-1.14.4-debug";
    public static final String KAMI_KANJI = "\u795E";
    private static final String KAMI_CONFIG_NAME_DEFAULT = "KAMIConfig.json5";

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

        try {
            this.config = constructConfiguration();
            loadConfiguration(config);
            KamiMod.log.info("Settings loaded");
        } catch (IOException | ValueDeserializationException e) {
            e.printStackTrace();
        }

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        FeatureManager.INSTANCE.getFeatures().stream().filter(AbstractFeature::isEnabled).forEach(AbstractFeature::enable);

        KamiMod.log.info(MODNAME + " initialised");
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
    
    private ConfigTree constructConfiguration() {
        MapConfigType<Bind, BigDecimal> bindType = ConfigTypes.makeMap(ConfigTypes.STRING, ConfigTypes.INTEGER).derive(Bind.class, map -> {
            boolean alt = map.getOrDefault("alt", 1) == 1;
            boolean ctrl = map.getOrDefault("ctrl", 1) == 1;
            boolean shift = map.getOrDefault("shift", 1) == 1;
            boolean keysm = map.getOrDefault("keysm", 1) == 1;
            int code = map.getOrDefault("code", -1);
            return new Bind(ctrl, alt, shift, InputUtil.getKeyCode(keysm ? code : -1, keysm ? -1 : code));
        }, bind -> {
            HashMap<String, Integer> map = new HashMap<>();
            map.put("alt", bind.isAlt() ? 0 : 1);
            map.put("ctrl", bind.isCtrl() ? 0 : 1);
            map.put("shift", bind.isShift() ? 0 : 1);
            map.put("keysm", (((IKeyBinding) bind.binding).getKeyCode().getCategory() == InputUtil.Type.KEYSYM) ? 0 : 1);
            map.put("code", ((IKeyBinding) bind.getBinding()).getKeyCode().getKeyCode());
            return map;
        });

        MapConfigType<GameProfile, String> profileType = ConfigTypes.makeMap(ConfigTypes.STRING, ConfigTypes.STRING).derive(GameProfile.class,
                map -> new GameProfile(UUID.fromString(map.get("uuid")), map.get("name")),
                profile -> {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("uuid", profile.getId().toString());
                    map.put("name", profile.getName());
                    return map;
                });

        ListConfigType<List<GameProfile>, Map<String, String>> friendsType = ConfigTypes.makeList(profileType);

        AnnotatedSettings settings = AnnotatedSettings.builder()
                .collectMembersRecursively()
                .collectOnlyAnnotatedMembers()
                .useNamingConvention(ProperCaseConvention.INSTANCE)
                .registerTypeMapping(Bind.class, bindType)
                .registerTypeMapping(GameProfile.class, profileType)
                .build();
        ConfigTreeBuilder builder = ConfigTree.builder();
        constructFriendsConfiguration(settings, builder);

        ConfigBranch friends = builder.fork("friends").applyFromPojo(Friends.INSTANCE, settings).build();
        PropertyMirror<List<GameProfile>> mirror = PropertyMirror.create(friendsType);
        mirror.mirror(friends.lookupLeaf("Friends", friendsType.getSerializedType()));
        Friends.mirror = mirror;

        return builder.build();
    }

    private void constructFriendsConfiguration(AnnotatedSettings settings, ConfigTreeBuilder builder) {
        ConfigTreeBuilder modules = builder.fork("features");
        // only full features because they have names
        FeatureManager.INSTANCE.getFullFeatures().forEach(f -> f.config = modules.fork(f.getName()).applyFromPojo(f, settings).build());
        // TODO: the rest of places that have @Settings
        modules.finishBranch();
    }

    public static void loadConfiguration(ConfigTree config) throws IOException, ValueDeserializationException {
        FiberSerialization.deserialize(config, Files.newInputStream(Paths.get(getConfigName())), new JanksonValueSerializer(false));
    }

    public static void saveConfiguration(ConfigTree config) throws IOException {
        if (config != null) // A crash while initialising config might cause config to be null, and an error in this line will drown out the error that caused minecraft to crash!
            FiberSerialization.serialize(config, Files.newOutputStream(Paths.get(getConfigName())), new JanksonValueSerializer(false));
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

    public ConfigTree getConfig() {
        return config;
    }

    public static KamiMod getInstance() {
        return INSTANCE;
    }

}
