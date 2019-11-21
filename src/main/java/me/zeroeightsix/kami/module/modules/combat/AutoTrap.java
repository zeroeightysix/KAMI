package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.zeroeightsix.kami.util.BlockInteractionHelper.checkForNeighbours;
import static me.zeroeightsix.kami.util.BlockInteractionHelper.faceVectorPacketInstant;

/**
 * Created 6 August 2019 by hub
 * Updated 21 November 2019 by hub
 */
@Module.Info(name = "AutoTrap", category = Module.Category.COMBAT)
public class AutoTrap extends Module {

    private final Vec3d[] offsetsDefault = {
            new Vec3d(0, 0, -1),
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 1, -1),
            new Vec3d(1, 1, 0),
            new Vec3d(0, 1, 1),
            new Vec3d(-1, 1, 0),
            new Vec3d(0, 2, -1),
            new Vec3d(1, 2, 0),
            new Vec3d(0, 2, 1),
            new Vec3d(-1, 2, 0),
            new Vec3d(0, 3, -1),
            new Vec3d(0, 3, 0)
    };

    private Setting<Double> range = register(Settings.d("Range", 5.5d));
    private Setting<Integer> blockPerTick = register(Settings.i("Blocks per Tick", 4));
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Boolean> announceUsage = register(Settings.b("Announce Usage", false));

    private EntityPlayer closestTarget;
    private String lastTickTargetName;

    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;

    private boolean isSneaking = false;


    private int offsetStep = 0;

    private boolean firstRun;

    @Override
    protected void onEnable() {

        if (mc.player == null) {
            this.disable();
            return;
        }

        firstRun = true;

        // save initial player hand
        playerHotbarSlot = Wrapper.getPlayer().inventory.currentItem;
        lastHotbarSlot = -1;

    }

    @Override
    protected void onDisable() {

        if (mc.player == null) {
            return;
        }

        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            Wrapper.getPlayer().inventory.currentItem = playerHotbarSlot;
        }

        if (isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }

        playerHotbarSlot = -1;
        lastHotbarSlot = -1;

        if (announceUsage.getValue()) {
            Command.sendChatMessage("[AutoTrap] Disabled!");
        }

    }

    @Override
    public void onUpdate() {

        if (mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }

        findClosestTarget();

        if (closestTarget == null) {
            if (firstRun) {
                firstRun = false;
                if (announceUsage.getValue()) {
                    Command.sendChatMessage("[AutoTrap] Enabled, waiting for target.");
                }
            }
            return;
        }

        if (firstRun) {
            firstRun = false;
            lastTickTargetName = closestTarget.getName();
            if (announceUsage.getValue()) {
                Command.sendChatMessage("[AutoTrap] Enabled, target: " + lastTickTargetName);
            }
        } else if (!lastTickTargetName.equals(closestTarget.getName())) {
            lastTickTargetName = closestTarget.getName();
            offsetStep = 0;
            if (announceUsage.getValue()) {
                Command.sendChatMessage("[AutoTrap] New target: " + lastTickTargetName);
            }
        }

        List<Vec3d> placeTargets = new ArrayList<>();
        Collections.addAll(placeTargets, offsetsDefault);

        int blocksPlaced = 0;

        while (blocksPlaced < blockPerTick.getValue()) {

            if (offsetStep >= placeTargets.size()) {
                offsetStep = 0;
                break;
            }

            BlockPos offsetPos = new BlockPos(placeTargets.get(offsetStep));
            BlockPos targetPos = new BlockPos(closestTarget.getPositionVector()).down().add(offsetPos.x, offsetPos.y, offsetPos.z);

            boolean shouldTryToPlace = true;

            // check if block is already placed
            if (!Wrapper.getWorld().getBlockState(targetPos).getMaterial().isReplaceable()) {
                shouldTryToPlace = false;
            }

            // check if entity blocks placing
            for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(targetPos))) {
                if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                    shouldTryToPlace = false;
                    break;
                }
            }

            if (shouldTryToPlace) {
                if (placeBlock(targetPos)) {
                    blocksPlaced++;
                }
            }

            offsetStep++;

        }

        if (blocksPlaced > 0) {

            if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
                Wrapper.getPlayer().inventory.currentItem = playerHotbarSlot;
                lastHotbarSlot = playerHotbarSlot;
            }

            if (isSneaking) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                isSneaking = false;
            }

        }

    }

    private boolean placeBlock(BlockPos pos) {

        // check if block at pos is replaceable
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            return false;
        }

        // check if we have a block adjacent to blockpos to click at
        if (!checkForNeighbours(pos)) {
            return false;
        }

        Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);

        for (EnumFacing side : EnumFacing.values()) {

            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();

            if (!mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false)) {
                continue;
            }

            Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));

            if (eyesPos.distanceTo(hitVec) > range.getValue()) {
                continue;
            }

            int obiSlot = findObiInHotbar();

            // check if any blocks were found
            if (obiSlot == -1) {
                this.disable();
                return false;
            }

            if (lastHotbarSlot != obiSlot) {
                Wrapper.getPlayer().inventory.currentItem = obiSlot;
                lastHotbarSlot = obiSlot;
            }

            Block neighborPos = mc.world.getBlockState(neighbor).getBlock();
            if (BlockInteractionHelper.blackList.contains(neighborPos) || BlockInteractionHelper.shulkerList.contains(neighborPos)) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                isSneaking = true;
            }

            // fake rotation
            if (rotate.getValue()) {
                faceVectorPacketInstant(hitVec);
            }

            // place block
            mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);

            return true;

        }

        return false;

    }

    private int findObiInHotbar() {

        // search blocks in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {

            // filter out non-block items
            ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block instanceof BlockObsidian) {
                slot = i;
                break;
            }

        }

        return slot;

    }

    private void findClosestTarget() {

        List<EntityPlayer> playerList = Wrapper.getWorld().playerEntities;

        closestTarget = null;

        for (EntityPlayer target : playerList) {

            if (target == mc.player) {
                continue;
            }

            if (Friends.isFriend(target.getName())) {
                continue;
            }

            if (!EntityUtil.isLiving(target)) {
                continue;
            }

            if ((target).getHealth() <= 0) {
                continue;
            }

            if (closestTarget == null) {
                closestTarget = target;
                continue;
            }

            if (Wrapper.getPlayer().getDistance(target) < Wrapper.getPlayer().getDistance(closestTarget)) {
                closestTarget = target;
            }

        }

    }

}
