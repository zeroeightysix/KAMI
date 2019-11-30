package me.zeroeightsix.kami.module.modules.misc;

import com.mojang.authlib.GameProfile;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.util.UUID;

/**
 * Created 10 August 2019 by hub
 * Updated 23 November 2019 by hub
 */
@Module.Info(name = "FakePlayer", category = Module.Category.MISC, description = "Spawns a fake Player")
public class FakePlayer extends Module {

    @Override
    protected void onEnable() {

        if (mc.world == null) {
            return;
        }

        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("e195d3d7-e6dc-456e-8393-e2f90816a1af"), "Hausemaster"));
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        mc.world.addEntityToWorld(-100, fakePlayer);

    }

    @Override
    protected void onDisable() {
        mc.world.removeEntityFromWorld(-100);
    }

}
