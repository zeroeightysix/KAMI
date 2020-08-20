package me.zeroeightsix.kami.feature.module;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zeroeightsix.kami.feature.FindFeature;
import me.zeroeightsix.kami.feature.FullFeature;
import me.zeroeightsix.kami.setting.SettingVisibility;
import net.minecraft.client.MinecraftClient;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@FindFeature(findDescendants = true)
public class Module extends FullFeature {

    private final Category category = getAnnotation().category();
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    @Setting(name = "Show in active modules")
    @SettingVisibility.Constant(false)
    public boolean showInActiveModules = true;

    public Module() {
        Module.Info annotation = getAnnotation();
        setName(annotation.name());
        setDisplayName(annotation.name());
        setDescription(annotation.description());
    }

    @Override
    public void initListening() {
        setAlwaysListening(getAnnotation().alwaysListening());
    }

    private Info getAnnotation() {
        if (getClass().isAnnotationPresent(Info.class)) {
            return getClass().getAnnotation(Info.class);
        }
        throw new IllegalStateException("No Annotation on class " + this.getClass().getCanonicalName() + "!");
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
