package me.zeroeightsix.kami.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.gui.old.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.old.rgui.component.Component;
import me.zeroeightsix.kami.gui.old.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.old.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.old.rgui.util.Docking;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by 086 on 17/11/2017.
 */
public class SettingsPool {

    private static final ArrayList<SettingsClass> classes = new ArrayList<>();

    public static void flushClass(SettingsClass settingsClass) {
        classes.add(settingsClass);
    }

    private static final HashMap<Class<? extends FieldConverter>, FieldConverter> converter_instances = new HashMap<>();

    public static void load(File file) {
        JsonObject rootObject = null;
        try (FileReader f = new FileReader(file)){
            rootObject = new JsonParser().parse(f).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        assert rootObject != null;
        JsonObject settingsObject = rootObject.get("settings").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : settingsObject.entrySet()) {
            String path = entry.getKey();
            String[] strings = path.split("~");
            if (strings.length != 2) continue;
            String classP = strings[0];
            String varName = strings[1];

            SettingsClass settingsClass = classes.stream().filter(settingsClass1 -> settingsClass1.getClass().getCanonicalName().equals(classP)).findFirst().orElse(null);
            if (settingsClass == null) {
                System.err.println("Couldn't find setting class " + classP + "!");
                continue;
            }
            SettingsClass.StaticSetting staticSetting = settingsClass.getSettingByFieldName(varName);
            if (staticSetting == null) {
                System.err.println("Couldn't find setting by fieldname " + varName + " in class " + classP + "!");
                continue;
            }

            Setting setting = staticSetting.getField().getAnnotation(Setting.class);
            JsonElement valueb = entry.getValue();
            Object value = null;
            try {
                FieldConverter converter = getConverter(setting.converter());
                value = converter.fromJson(staticSetting, valueb);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("failed to convert: " + staticSetting.field + " -> " + setting.converter().getSimpleName() + " -> " + staticSetting.getField().getType());
            }
            staticSetting.setValue(value);
        }

        try {
            Command.COMMAND_PREFIX = String.valueOf(rootObject.get("command_prefix").getAsString());
        }catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject guiMap = rootObject.get("gui").getAsJsonObject();
        guiMap.entrySet().forEach(stringJsonElementEntry -> {
            String key = stringJsonElementEntry.getKey();
            JsonElement element = stringJsonElementEntry.getValue();

            String strings[] = key.split(Pattern.quote("."));
            String framename = strings[0];

            Component targetFrame = KamiMod.getInstance().getGuiManager().getChildren().stream()
                    .filter(component -> (component instanceof Frame) && (((Frame)component).getTitle().equals(framename)))
                    .findFirst()
                    .orElse(null);
            if (targetFrame == null) {
                KamiMod.log.info("Missing frame " + framename + "! (" + key + ")");
            }else{
                Frame frame = (Frame) targetFrame;
                switch (strings[1]) {
                    case "x":
                        frame.setX(element.getAsInt());
                        break;
                    case "y":
                        frame.setY(element.getAsInt());
                        break;
                    case "pinned":
                        frame.setPinned(element.getAsBoolean());
                        break;
                    case "minimized":
                        frame.setMinimized(element.getAsBoolean());
                        break;
                    case "docking":
                        frame.setDocking(Docking.values()[element.getAsInt()]);
                        if (frame.getDocking().isLeft()) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.LEFT);
                        else if (frame.getDocking().isRight()) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.RIGHT);
                        break;
                }
            }
        });
        KamiMod.getInstance().getGuiManager().getChildren().stream().filter(component -> (component instanceof Frame) && (((Frame) component).isPinneable()) && component.isVisible()).forEach(component -> component.setOpacity(0f));
    }

    public static void save(File file) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject settings = new JsonObject();
        classes.forEach(settingsClass ->
            settingsClass.settings.forEach(staticSetting -> {
                Object value = staticSetting.getValue();
                JsonElement jsonElement = null;
                Setting setting = staticSetting.getField().getAnnotation(Setting.class);
                try {
                    FieldConverter converter = getConverter(setting.converter());
                    jsonElement = converter.toJson(staticSetting);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("FAILED TO CONVERT TO SAVE REPRESENTATION (" + staticSetting.field + " -> " + setting.converter().getSimpleName() + " -> " + staticSetting.getField().getType());
                }
                settings.add(staticSetting.getFullName(), jsonElement);
            }
            )
        );
        root.add("settings", settings);

        JsonObject gui = new JsonObject();
        KamiMod.getInstance().getGuiManager().getChildren().stream().filter(component -> component instanceof Frame).forEach(component -> {
            Frame frame = (Frame) component;
            gui.add(frame.getTitle() + ".x", new JsonPrimitive(frame.getX()));
            gui.add(frame.getTitle() + ".y", new JsonPrimitive(frame.getY()));
            gui.add(frame.getTitle() + ".pinned", new JsonPrimitive(frame.isPinned()));
            gui.add(frame.getTitle() + ".minimized", new JsonPrimitive(frame.isMinimized()));
            gui.add(frame.getTitle() + ".docking", new JsonPrimitive(frame.getDocking().ordinal()));
        });
        root.add("gui", gui);

        root.add("command_prefix", new JsonPrimitive(Command.COMMAND_PREFIX));

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(root.toString());
        writer.close();
    }

    private static FieldConverter getConverter(Class<? extends FieldConverter> converter) {
        if (!converter_instances.containsKey(converter)) {
            try {
                converter_instances.put(converter, converter.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return converter_instances.get(converter);
    }

}
