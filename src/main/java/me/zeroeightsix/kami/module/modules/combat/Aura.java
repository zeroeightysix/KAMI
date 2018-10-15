package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.misc.AutoTool;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;

/**
 * Created by 086 on 12/12/2017.
 */
@Module.Info(name = "Aura", category = Module.Category.COMBAT, description = "Hits entities around you")
public class Aura extends Module {

    private Setting<Boolean> players = register(Settings.b("Players", true));
    private Setting<Boolean> animals = register(Settings.b("Animals", false));
    private Setting<Boolean> mobs = register(Settings.b("Mobs", false));
    private Setting<Double> range = register(Settings.d("Range", 5.5d));
    private Setting<Boolean> wait = register(Settings.b("Wait", true));
    private Setting<Boolean> walls = register(Settings.b("Walls", false));

    @Override
    public void onUpdate() {
        if (mc.player.isDead) return;
        boolean shield = mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == EnumHand.OFF_HAND;
        if (mc.player.isHandActive() && !shield) return;

        if (wait.getValue())
            if (mc.player.getCooledAttackStrength(getLagComp()) < 1) return;
            else if (mc.player.ticksExisted % 2 != 0) return;

        Iterator<Entity> entityIterator = Minecraft.getMinecraft().world.loadedEntityList.iterator();
        while (entityIterator.hasNext()) {
            Entity target = entityIterator.next();
            if (!EntityUtil.isLiving(target)) continue;
            if (target == mc.player) continue;
            if (mc.player.getDistance(target) > range.getValue()) continue;
            if (((EntityLivingBase) target).getHealth() <= 0) continue;
            if (((EntityLivingBase) target).hurtTime != 0 && wait.getValue()) continue;
            if (!walls.getValue() && (!mc.player.canEntityBeSeen(target) && !canEntityFeetBeSeen(target)))
                continue; // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed
            if (players.getValue() && target instanceof EntityPlayer && !Friends.isFriend(target.getName())) {
                attack(target);
                return;
            } else {
                if (EntityUtil.isPassive(target) ? animals.getValue() : (EntityUtil.isMobAggressive(target) && mobs.getValue())) {
                    if (ModuleManager.isModuleEnabled("AutoTool")) AutoTool.equipBestWeapon();
                    attack(target);
                    return;
                }
            }
        }
    }

    private void attack(Entity e) {
        mc.playerController.attackEntity(mc.player, e);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    private float getLagComp() {
        if (wait.getValue())
            return -(20 - LagCompensator.INSTANCE.getTickRate());
        return 0.0F;
    }

    private boolean canEntityFeetBeSeen(Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posX + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }
}
