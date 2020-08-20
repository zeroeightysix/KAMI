package me.zeroeightsix.kami.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.World;

public class Wrapper {

    public static MinecraftClient getMinecraft() {
        return MinecraftClient.getInstance();
    }
    public static ClientPlayerEntity getPlayer() {
        return getMinecraft().player;
    }
    public static World getWorld() {
        return getMinecraft().world;
    }

}
