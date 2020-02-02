package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.FeatureManager;
import me.zeroeightsix.kami.module.modules.misc.AutoTool;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RayTraceContext;

/**
 * Created by 086 on 12/12/2017.
 * Updated by hub on 31 October 2019
 */
@Module.Info(name = "Aura", category = Module.Category.COMBAT, description = "Hits entities around you")
public class Aura extends Module {

    private Setting<Boolean> attackPlayers = register(Settings.b("Players", true));
    private Setting<Boolean> attackMobs = register(Settings.b("Mobs", false));
    private Setting<Boolean> attackAnimals = register(Settings.b("Animals", false));
    private Setting<Double> hitRange = register(Settings.d("Hit Range", 5.5d));
    private Setting<Boolean> ignoreWalls = register(Settings.b("Ignore Walls", true));
    private Setting<WaitMode> waitMode = register(Settings.e("Mode", WaitMode.DYNAMIC));
    private Setting<Integer> waitTick = register(Settings.integerBuilder("Tick Delay").withMinimum(0).withValue(3).withVisibility(o -> waitMode.getValue().equals(WaitMode.STATIC)).build());
    private Setting<Boolean> switchTo32k = register(Settings.b("32k Switch", true));
    private Setting<Boolean> onlyUse32k = register(Settings.b("32k Only", false));

    private int waitCounter;

    @Override
    public void onUpdate() {
        if (!mc.player.isAlive()) {
            return;
        }

        boolean shield = mc.player.getOffHandStack().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == Hand.OFF_HAND;
        if (mc.player.isUsingItem() && !shield) {
            return;
        }

        if (waitMode.getValue().equals(WaitMode.DYNAMIC)) {
            if (mc.player.getAttackCooldownProgress(getLagComp()) < 1) { // TODO: Is the right function?
                return;
            } else if (mc.player.age % 2 != 0) {
                return;
            }
        }

        if (waitMode.getValue().equals(WaitMode.STATIC) && waitTick.getValue() > 0) {
            if (waitCounter < waitTick.getValue()) {
                waitCounter++;
                return;
            } else {
                waitCounter = 0;
            }
        }

        for (Entity target : MinecraftClient.getInstance().world.getEntities()) {
            if (!EntityUtil.isLiving(target)) {
                continue;
            }
            if (target == mc.player) {
                continue;
            }
            if (mc.player.distanceTo(target) > hitRange.getValue()) {
                continue;
            }
            if (((LivingEntity) target).getHealth() <= 0) {
                continue;
            }
            if (waitMode.getValue().equals(WaitMode.DYNAMIC) && ((LivingEntity) target).hurtTime != 0) {
                continue;
            }
            if (!ignoreWalls.getValue() && (!mc.player.canSee(target) && !canEntityFeetBeSeen(target))) {
                continue; // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed
            }
            if (attackPlayers.getValue() && target instanceof PlayerEntity && !Friends.isFriend(target.getName().getString())) {
                attack(target);
                return;
            } else {
                if (EntityUtil.isPassive(target) ? attackAnimals.getValue() : (EntityUtil.isMobAggressive(target) && attackMobs.getValue())) {
                    // We want to skip this if switchTo32k.getValue() is true,
                    // because it only accounts for tools and weapons.
                    // Maybe someone could refactor this later? :3
                    if (!switchTo32k.getValue() && FeatureManager.isModuleEnabled("AutoTool")) {
                        AutoTool.equipBestWeapon();
                    }
                    attack(target);
                    return;
                }
            }
        }

    }

    private boolean checkSharpness(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            return false;
        }

        ListTag enchantments = stack.getEnchantments();

        for(int i = 0; i < enchantments.size(); ++i) {
            CompoundTag enchantment = enchantments.getCompoundTag(i);
            if (enchantment.getInt("id") == 16) { // id of sharpness
                int lvl = enchantment.getInt("lvl");
                if (lvl >= 34)
                    return true;
                break; // we've already found sharpness; no other enchant will match id == 16
            }
        }

        return false;
    }

    private void attack(Entity e) {

        boolean holding32k = false;

        if (checkSharpness(mc.player.getActiveItem())) {
            holding32k = true;
        }

        if (switchTo32k.getValue() && !holding32k) {

            int newSlot = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getInvStack(i);
                if (stack == ItemStack.EMPTY) {
                    continue;
                }
                if (checkSharpness(stack)) {
                    newSlot = i;
                    break;
                }
            }

            if (newSlot != -1) {
                mc.player.inventory.selectedSlot = newSlot;
                holding32k = true;
            }

        }

        if (onlyUse32k.getValue() && !holding32k) {
            return;
        }

        mc.interactionManager.attackEntity(mc.player, e);
        mc.player.swingHand(Hand.MAIN_HAND);

    }

    private float getLagComp() {
        if (waitMode.getValue().equals(WaitMode.DYNAMIC)) {
            return -(20 - LagCompensator.INSTANCE.getTickRate());
        }
        return 0.0F;
    }

    private boolean canEntityFeetBeSeen(Entity entityIn) {
        RayTraceContext context = new RayTraceContext(mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0), entityIn.getPos(), RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, mc.player);
        return mc.world.rayTrace(context).getType() == HitResult.Type.MISS;
    }

    private enum WaitMode {
        DYNAMIC, STATIC
    }

}
