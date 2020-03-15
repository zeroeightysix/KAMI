package me.zeroeightsix.kami.feature.module;

import me.zeroeightsix.kami.feature.FullFeature;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.MinecraftClient;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 086 on 23/08/2017.
 * Updated by hub on 3 November 2019
 */
public class Module extends FullFeature {

    private final Category category = getAnnotation().category();
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public Setting<Boolean> showInActiveModules = register(Settings.booleanBuilder("Show in active modules")
            .withVisibility(aBoolean -> false)
            .withValue(true)
            .build());

    public Module() {
        setAlwaysListening(getAnnotation().alwaysListening());
        setOriginalName(getAnnotation().name());
        getName().setValue(getAnnotation().name());
        setDescription(getAnnotation().description());
    }

    private Info getAnnotation() {
        if (getClass().isAnnotationPresent(Info.class)) {
            return getClass().getAnnotation(Info.class);
        }
        throw new IllegalStateException("No Annotation on class " + this.getClass().getCanonicalName() + "!");
    }

    public void setName(String name) {
        this.getName().setValue(name);
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
        Module.Category category();
        boolean alwaysListening() default false;
    }

    public Category getCategory() {
        return category;
    }

}
