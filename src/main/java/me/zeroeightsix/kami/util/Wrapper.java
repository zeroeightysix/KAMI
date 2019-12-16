package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.mixin.client.IEntityRenderDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sun.java2d.pipe.TextRenderer;

/**
 * Created by 086 on 11/11/2017.
 */
public class Wrapper {

    private static TextRenderer textRenderer;

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
        IEntityRenderDispatcher dispatcher = (IEntityRenderDispatcher) getMinecraft().getEntityRenderManager();
        return new Vec3d(dispatcher.getRenderPosX(), dispatcher.getRenderPosY(), dispatcher.getRenderPosZ());
    }

}
