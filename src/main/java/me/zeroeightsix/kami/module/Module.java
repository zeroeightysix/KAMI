package me.zeroeightsix.kami.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.modules.movement.Sprint;
import me.zeroeightsix.kami.setting.FieldConverter;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.SettingsClass;
import me.zeroeightsix.kami.util.Bind;
import net.minecraft.client.Minecraft;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 086 on 23/08/2017.
 */
public class Module extends SettingsClass {

    private final String name = getAnnotation().name();
    private final String description = getAnnotation().description();
    private final Category category = getAnnotation().category();
    @Setting(name = "Bind", hidden = true, converter = BindsConverter.class)
    private Bind bind = Bind.none();
    @Setting(name = "Enabled", hidden = true)
    private boolean enabled;
    public boolean alwaysListening = false;
    protected static final Minecraft mc = Minecraft.getMinecraft();

    public Module() {
        alwaysListening = getAnnotation().alwaysListening();

        enabled = false;
//        FMLCommonHandler.instance().bus().register(this);
        initSettings();
    }

    private Info getAnnotation() {
        return getClass().isAnnotationPresent(Info.class) ? getClass().getAnnotation(Info.class) : Sprint.class.getAnnotation(Info.class); // dummy annotation
    }

    public void onUpdate() {}
    public void onRender() {}
    public void onWorldRender(RenderEvent event) {}

    public Bind getBind() {
        return bind;
    }

    public String getBindName() {
        return bind.toString();
    }

    public enum Category
    {
        COMBAT("Combat", false),
        EXPLOITS("Exploits", false),
        RENDER("Render", false),
        MISC("Misc", false),
        PLAYER("Player", false),
        MOVEMENT("Movement", false),
        HIDDEN("Hidden", true);

        boolean hidden;
        String name;
        private int bind;

        Category(String name, boolean hidden) {
            this.name = name;
            this.hidden = hidden;
        }

        public boolean isHidden() {
            return hidden;
        }

        public String getName() {
            return name;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info
    {
        String name();
        String description() default "Descriptionless";
        Module.Category category();
        boolean alwaysListening() default false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void enable() {
        enabled = true;
        onEnable();
        if (!alwaysListening)
            KamiMod.EVENT_BUS.subscribe(this);
    }

    public void disable() {
        enabled = false;
        onDisable();
        if (!alwaysListening)
            KamiMod.EVENT_BUS.unsubscribe(this);
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public void setEnabled(boolean enabled) {
        boolean prev = this.enabled;
        if (prev != enabled)
            if (enabled)
                enable();
            else
                disable();
    }

    public String getHudInfo() {
        return null;
    }

    protected final void setAlwaysListening(boolean alwaysListening) {
        this.alwaysListening = alwaysListening;
        if (alwaysListening) KamiMod.EVENT_BUS.subscribe(this);
        if (!alwaysListening && isDisabled()) KamiMod.EVENT_BUS.unsubscribe(this);
    }

    /**
     * Cleanup method in case this module wants to do something when the client closes down
     */
    public void destroy(){};

    public static class BindsConverter implements FieldConverter {

        public BindsConverter() {
        }

        @Override
        public JsonElement toJson(StaticSetting setting) {
            Bind bind = (Bind) setting.getValue();
            if (bind.isEmpty()) return null;
            JsonObject object = new JsonObject();
            object.add("shift", new JsonPrimitive(bind.isShift()));
            object.add("alt", new JsonPrimitive(bind.isAlt()));
            object.add("ctrl", new JsonPrimitive(bind.isCtrl()));
            object.add("key", new JsonPrimitive(bind.getKey()));
            return object;
        }

        @Override
        public Object fromJson(StaticSetting setting, JsonElement value) {
            if (value == null || value.isJsonNull()) return Bind.none();
            JsonObject object = value.getAsJsonObject();
            boolean shift = object.get("shift").getAsBoolean();
            boolean alt = object.get("alt").getAsBoolean();
            boolean ctrl = object.get("ctrl").getAsBoolean();
            int key = object.get("key").getAsInt();
            return new Bind(ctrl, alt, shift, key);
        }
    }

}
