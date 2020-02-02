package me.zeroeightsix.kami.module;

import com.mojang.blaze3d.platform.GlStateManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.event.events.RenderHudEvent;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.gui.KamiGuiScreen;
import me.zeroeightsix.kami.gui.KamiHud;
import me.zeroeightsix.kami.mixin.client.IKeyBinding;
import me.zeroeightsix.kami.module.modules.ClickGUI;
import me.zeroeightsix.kami.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by 086 on 23/08/2017.
 */
public class ModuleManager {

    public static ArrayList<Module> modules = new ArrayList<>();

    /**
     * Lookup map for getting by name
     */
    static HashMap<String, Module> lookup = new HashMap<>();

    @EventHandler
    public Listener<TickEvent.Client> clientTickListener = new Listener<>(event -> {
        if (Wrapper.getPlayer() == null) return;
        onUpdate();
    });

    @EventHandler
    public Listener<RenderEvent.World> worldRenderListener = new Listener<>(event -> {
        onWorldRender(event);
        KamiTessellator.releaseGL();
    });

    @EventHandler
    public Listener<RenderHudEvent> renderHudEventListener = new Listener<>(event -> {
        onRender();

        if (!(MinecraftClient.getInstance().currentScreen instanceof KamiGuiScreen)) {
            KamiHud.INSTANCE.renderHud();
        }
//        KamiTessellator.releaseGL();
    });

    public static void updateLookup() {
        lookup.clear();
        for (Module m : modules)
            lookup.put(m.getName().toLowerCase(), m);
    }

    public static ModuleManager initialize() {
        Set<Class> classList = ClassFinder.findClasses(ClickGUI.class.getPackage().getName(), Module.class);
        classList.forEach(aClass -> {
            try {
                Module module = (Module) aClass.getConstructor().newInstance();
                modules.add(module);
                // lookup.put(module.getName().toLowerCase(), module);
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
                System.err.println("Couldn't initiate module " + aClass.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Couldn't initiate module " + aClass.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
            }
        });
        KamiMod.log.info("Modules initialised");
        getModules().sort(Comparator.comparing(Module::getName));

        return new ModuleManager();
    }

    public static void onUpdate() {
        modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(module -> module.onUpdate());
    }

    public static void onRender() {
        modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(module -> module.onRender());
    }

    public static void onWorldRender(RenderEvent.World event) {
        MinecraftClient.getInstance().getProfiler().push("kami");

        MinecraftClient.getInstance().getProfiler().push("setup");
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepthTest();

        GlStateManager.lineWidth(1f);
        Vec3d renderPos = EntityUtil.getInterpolatedPos(Wrapper.getPlayer(), event.getPartialTicks());

        MinecraftClient.getInstance().getProfiler().pop();

        modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(module -> {
            MinecraftClient.getInstance().getProfiler().push(module.getName());
            module.onWorldRender(event);
            MinecraftClient.getInstance().getProfiler().pop();
        });

        MinecraftClient.getInstance().getProfiler().push("release");
        GlStateManager.lineWidth(1f);

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
        GlStateManager.enableDepthTest();
        GlStateManager.enableCull();
        KamiTessellator.releaseGL();
        MinecraftClient.getInstance().getProfiler().pop();

        MinecraftClient.getInstance().getProfiler().pop();
    }

    public static void onBind(int key, int scancode, int i) {
        boolean pressed = i != 0;
        InputUtil.KeyCode code = InputUtil.getKeyCode(key, scancode);

        if (Wrapper.getMinecraft().currentScreen != null) {
            return;
        }

        if (key == 89 && scancode == 29) {
            if (KamiMod.getInstance().kamiGuiScreen == null) {
                KamiMod.getInstance().kamiGuiScreen = new KamiGuiScreen();
            }
            Wrapper.getMinecraft().openScreen(KamiMod.getInstance().kamiGuiScreen);
        }

        modules.forEach(module -> {
            Bind bind = module.getBind();
            if (((IKeyBinding) bind.getBinding()).getKeyCode().equals(code)) {
                ((IKeyBinding) bind.getBinding()).setPressed(pressed);
            }
            if (module.getBind().isDown()) {
                module.toggle();
            }
        });
    }

    public static ArrayList<Module> getModules() {
        return modules;
    }

    public static Module getModuleByName(String name) {
        return lookup.get(name.toLowerCase());
//        return getModules().stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static boolean isModuleEnabled(String moduleName) {
        Module m = getModuleByName(moduleName);
        if (m == null) return false;
        return m.isEnabled();
    }
}
