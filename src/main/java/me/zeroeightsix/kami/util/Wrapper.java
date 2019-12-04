package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.mixin.duck.HasRenderPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;

/**
 * Created by 086 on 11/11/2017.
 */
public class Wrapper {

    private static FontRenderer fontRenderer;

    public static void init() {
//      fontRenderer = new CFontRenderer(new Font("Segoe UI", Font.PLAIN, 19), true, false);
        fontRenderer = KamiGUI.fontRenderer;
    }
    public static MinecraftClient getMinecraft() {
        return MinecraftClient.getInstance();
    }
    public static ClientPlayerEntity getPlayer() {
        return getMinecraft().player;
    }
    public static World getWorld() {
        return getMinecraft().world;
    }

    public static Vec3d getRenderPosition() {
        HasRenderPosition hrp = (HasRenderPosition) getMinecraft().getEntityRenderManager();
        return new Vec3d(hrp.getRenderPosX(), hrp.getRenderPosY(), hrp.getRenderPosZ());
    }

    public static FontRenderer getFontRenderer() {
        return fontRenderer;
    }
}
