package me.zeroeightsix.kami.module;

import com.google.common.base.Converter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import glm_.vec2.Vec2;
import imgui.ImGui;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Bind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 086 on 23/08/2017.
 * Updated by hub on 3 November 2019
 */
public class ModulePlay extends Module {

    private final Category category = getAnnotation().category();
    private Setting<Bind> bind = register(Settings.custom("Bind", Bind.none(), new BindConverter(), setting -> {
        ImGui.INSTANCE.text("Bound to " + getBind().toString()); // TODO: Highlight bind in another color?
        ImGui.INSTANCE.sameLine(0, -1);
        if (ImGui.INSTANCE.button("Bind", new Vec2())) {
            // TODO: Bind popup?
            // Maybe just display "Press a key" instead of the normal "Bound to ...", and wait for a key press.
        }
    }).build());
    public boolean alwaysListening;
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public ModulePlay() {
        alwaysListening = getAnnotation().alwaysListening();
        registerAll(bind, getEnabled());
        getName().setValue(getAnnotation().name());
        setDescription(getAnnotation().description());
    }

    private Info getAnnotation() {
        if (getClass().isAnnotationPresent(Info.class)) {
            return getClass().getAnnotation(Info.class);
        }
        throw new IllegalStateException("No Annotation on class " + this.getClass().getCanonicalName() + "!");
    }

    public void onUpdate() {}
    public void onRender() {}
    public void onWorldRender(RenderEvent event) {}

    public Bind getBind() {
        return bind.getValue();
    }

    public void setName(String name) {
        this.getName().setValue(name);
        ModuleManager.updateLookup();
    }

    public enum Category {
        COMBAT("Combat", false),
        RENDER("Render", false),
        MISC("Misc", false),
        PLAYER("Player", false),
        MOVEMENT("Movement", false),
        HIDDEN("Hidden", true);

        boolean hidden;
        String name;

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
        ModulePlay.Category category();
        boolean alwaysListening() default false;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return getEnabled().getValue();
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    @Override
    public boolean enable() {
        if (super.enable()) {
            if (!alwaysListening)
                KamiMod.EVENT_BUS.subscribe(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean disable() {
        if (super.disable()) {
            if (!alwaysListening)
                KamiMod.EVENT_BUS.unsubscribe(this);
            return true;
        }
        return false;
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public void setEnabled(boolean enabled) {
        boolean prev = this.getEnabled().getValue();
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

    protected void registerAll(Setting... settings) {
        for (Setting setting : settings) {
            register(setting);
        }
    }

    private class BindConverter extends Converter<Bind, JsonElement> {
        @Override
        protected JsonElement doForward(Bind bind) {
            JsonArray array = new JsonArray();
            array.add(bind.isAlt());
            array.add(bind.isCtrl());
            array.add(bind.isShift());
//            array.add(bind.getKey());
//            array.add(bind.getScancode());
            //TODO
            return array;
        }

        @Override
        protected Bind doBackward(JsonElement jsonElement) {
            JsonArray array = jsonElement.getAsJsonArray();
            boolean alt = array.get(0).getAsBoolean();
            boolean ctrl = array.get(1).getAsBoolean();
            boolean shift = array.get(2).getAsBoolean();
            int key = array.get(2).getAsInt();
            int scancode = array.get(3).getAsInt();
            return new Bind(ctrl, alt, shift, InputUtil.getKeyCode(key, scancode));
        }
    }
}
