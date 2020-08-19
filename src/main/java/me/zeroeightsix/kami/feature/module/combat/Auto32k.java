package me.zeroeightsix.kami.feature.module.combat;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.command.Command;
import me.zeroeightsix.kami.feature.module.Aura;
import me.zeroeightsix.kami.feature.module.Freecam;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.ShulkerBoxCommon;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static me.zeroeightsix.kami.feature.module.player.Scaffold.faceVectorPacketInstant;

/**
 * Created by hub on 7 August 2019
 * Updated by hub on 31 October 2019
 */
@Module.Info(name = "Auto32k", category = Module.Category.COMBAT, description = "Do not use with any AntiGhostBlock Mod!")
public class Auto32k extends Module {

    private static final List<Block> blackList = Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL,
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.OAK_TRAPDOOR,
            Blocks.SPRUCE_TRAPDOOR,
            Blocks.BIRCH_TRAPDOOR,
            Blocks.JUNGLE_TRAPDOOR,
            Blocks.ACACIA_TRAPDOOR,
            Blocks.DARK_OAK_TRAPDOOR
    );

    private static final DecimalFormat df = new DecimalFormat("#.#");

    @Setting(name = "Move 32k to Hotbar")
    private boolean moveToHotbar = true;
    @Setting(name = "Auto enable Hit Aura")
    private boolean autoEnableHitAura = true;
    @Setting(name = "Place Range")
    private double placeRange = 4.0d;
    @Setting(name = "Y Offset (Hopper)")
    private int yOffset = 2;
    @Setting(name = "Place close to enemy")
    private boolean placeCloseToEnemy = false;
    @Setting(name = "Place Obi on Top")
    private boolean placeObiOnTop = true;
    @Setting(name = "Debug Messages")
    private boolean debugMessages = false;

    private int swordSlot;
    private static boolean isSneaking;

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (Freecam.INSTANCE.getEnabled()) {
            return;
        }

        if (!(mc.currentScreen instanceof HandledScreen)) {
            return;
        }

        if (!moveToHotbar) {
            this.disable();
            return;
        }

        if (swordSlot == -1) {
            return;
        }

        boolean swapReady = true;

        ScreenHandler container = ((HandledScreen) mc.currentScreen).getScreenHandler();

        if (container.getSlot(0).getStack().isEmpty()) {
            swapReady = false;
        }

        if (!(container.getSlot(swordSlot).getStack().isEmpty())) {
            swapReady = false;
        }

        if (swapReady) {
            mc.interactionManager.clickSlot(container.syncId, 0, swordSlot - 32, SlotActionType.SWAP, mc.player);
            if (autoEnableHitAura) {
                Aura.INSTANCE.enable();
            }
            this.disable();
        }
    });

    @Override
    public void onEnable() {
        if (mc.player == null || Freecam.INSTANCE.getEnabled()) {
            this.disable();
            return;
        }

        df.setRoundingMode(RoundingMode.CEILING);

        int hopperSlot = -1;
        int shulkerSlot = -1;
        int obiSlot = -1;
        swordSlot = -1;

        for (int i = 0; i < 9; i++) {

            if (hopperSlot != -1 && shulkerSlot != -1 && obiSlot != -1) {
                break;
            }

            ItemStack stack = mc.player.inventory.getStack(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof BlockItem)) {
                continue;
            }

            Block block = ((BlockItem) stack.getItem()).getBlock();

            if (block == Blocks.HOPPER) {
                hopperSlot = i;
            } else if (ShulkerBoxCommon.isShulkerBox(stack.getItem())) {
                shulkerSlot = i;
            } else if (block == Blocks.OBSIDIAN) {
                obiSlot = i;
            }

        }

        if (hopperSlot == -1) {
            if (debugMessages) {
                Command.sendChatMessage("[Auto32k] Hopper missing, disabling.");
            }
            this.disable();
            return;
        }

        if (shulkerSlot == -1) {
            if (debugMessages) {
                Command.sendChatMessage("[Auto32k] Shulker missing, disabling.");
            }
            this.disable();
            return;
        }

        int range = (int) Math.ceil(placeRange);

        CrystalAura crystalAura = CrystalAura.INSTANCE;
        //List<BlockPos> placeTargetList = crystalAura.getSphere(getPlayerPos(), range, range, false, true, 0);
        List<BlockPos> placeTargetList = new ArrayList<>(); // TODO

        Map<BlockPos, Double> placeTargetMap = new HashMap<>();

        BlockPos placeTarget = null;
        boolean useRangeSorting = false;

        for (BlockPos placeTargetTest : placeTargetList) {
            for (Entity entity : mc.world.getEntities()) {

                if (!(entity instanceof PlayerEntity)) {
                    continue;
                }

                if (entity == mc.player) {
                    continue;
                }

                if (Friends.isFriend(entity.getName().getString())) {
                    continue;
                }

                if (yOffset != 0) {
                    if (Math.abs(mc.player.getPos().y - placeTargetTest.getY()) > Math.abs(yOffset)) {
                        continue;
                    }
                }

                if (isAreaPlaceable(placeTargetTest)) {
                    double distanceToEntity = Math.sqrt(entity.squaredDistanceTo(placeTargetTest.getX(), placeTargetTest.getY(), placeTargetTest.getZ()));
                    // Add distance to Map Value of placeTarget Key
                    placeTargetMap.put(placeTargetTest, placeTargetMap.containsKey(placeTargetTest) ? placeTargetMap.get(placeTargetTest) + distanceToEntity : distanceToEntity);
                    useRangeSorting = true;
                }

            }
        }

        if (placeTargetMap.size() > 0) {

            placeTargetMap.forEach((k, v) -> {
                if (!isAreaPlaceable(k)) {
                    placeTargetMap.remove(k);
                }
            });

            if (placeTargetMap.size() == 0) {
                useRangeSorting = false;
            }

        }

        if (useRangeSorting) {

            if (placeCloseToEnemy) {
                if (debugMessages) {
                    Command.sendChatMessage("[Auto32k] Placing close to Enemy");
                }
                // Get Key with lowest Value (closest to enemies)
                placeTarget = Collections.min(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            } else {
                if (debugMessages) {
                    Command.sendChatMessage("[Auto32k] Placing far from Enemy");
                }
                // Get Key with highest Value (furthest away from enemies)
                placeTarget = Collections.max(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            }

        } else {

            if (debugMessages) {
                Command.sendChatMessage("[Auto32k] No enemy nearby, placing at first valid position.");
            }

            // Use any place target position if no enemies are around
            for (BlockPos pos : placeTargetList) {
                if (isAreaPlaceable(pos)) {
                    placeTarget = pos;
                    break;
                }
            }

        }

        if (placeTarget == null) {
            if (debugMessages) {
                Command.sendChatMessage("[Auto32k] No valid position in range to place!");
            }
            this.disable();
            return;
        }

        if (debugMessages) {
            Command.sendChatMessage("[Auto32k] Place Target: " + placeTarget.getX() + " " + placeTarget.getY() + " " + placeTarget.getZ() + " Distance: " + df.format(mc.player.getPos().distanceTo(new Vec3d(placeTarget.getX(),placeTarget.getY(),placeTarget.getZ()))));
        }

        mc.player.inventory.selectedSlot = hopperSlot;
        placeBlock(new BlockPos(placeTarget));

        mc.player.inventory.selectedSlot = shulkerSlot;
        placeBlock(new BlockPos(placeTarget.add(0, 1, 0)));

        if (placeObiOnTop && obiSlot != -1) {
            mc.player.inventory.selectedSlot = obiSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 2, 0)));
        }

        if (isSneaking) {
            mc.player.networkHandler.getConnection().send(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            isSneaking = false;
        }

        mc.player.inventory.selectedSlot = shulkerSlot;
        BlockPos hopperPos = new BlockPos(placeTarget);
        mc.player.networkHandler.getConnection().send(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(new Vec3d(0, 0, 0), Direction.DOWN, hopperPos, false)));
        swordSlot = shulkerSlot + 32;

    }

    private boolean isAreaPlaceable(BlockPos blockPos) {
        for (Entity entity : mc.world.getOtherEntities(null, new Box(blockPos), EntityPredicates.VALID_ENTITY)) {
            return false; // entity on block
        }

        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) {
            return false; // no space for hopper
        }

        if (!mc.world.getBlockState(blockPos.add(0, 1, 0)).getMaterial().isReplaceable()) {
            return false; // no space for shulker
        }

        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof AirBlock) {
            return false; // air below hopper
        }

        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof FluidBlock) {
            return false; // liquid below hopper
        }

        if (mc.player.getPos().distanceTo(new Vec3d(blockPos.getX(),blockPos.getY(),blockPos.getZ())) > placeRange) {
            return false; // out of range
        }

        Block block = mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock();
        if (blackList.contains(block) || ShulkerBoxCommon.isShulkerBox(block)) {
            return false; // would need sneak
        }

        return !(mc.player.getPos().distanceTo(new Vec3d(blockPos.getX(),blockPos.getY(),blockPos.getZ()).add(0, 1, 0)) > placeRange); // out of range

    }

    private static void placeBlock(BlockPos pos) {

        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            return;
        }

        // check if we have a block adjacent to blockpos to click at
        if (!checkForNeighbours(pos)) {
            return;
        }

        for (Direction side : Direction.values()) {

            BlockPos neighbor = pos.offset(side);
            Direction side2 = side.getOpposite();

//            if (!mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false)) {
//                continue;
//            }

            Vec3d hitVec = new Vec3d(neighbor.getX(),neighbor.getY(),neighbor.getZ()).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getVector().getX(),side2.getVector().getY(),side2.getVector().getZ()).multiply(0.5));

            Block neighborPos = mc.world.getBlockState(neighbor).getBlock();
            if (blackList.contains(neighborPos) || ShulkerBoxCommon.isShulkerBox(neighborPos)) {
                assert mc.player != null;
                mc.player.networkHandler.getConnection().send(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                isSneaking = true;
            }

            faceVectorPacketInstant(hitVec);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(hitVec, side2, neighbor, false));
            mc.player.swingHand(Hand.MAIN_HAND);
            ((IMinecraftClient) mc).setItemUseCooldown(4);

            return;
        }

    }

    private static boolean checkForNeighbours(BlockPos blockPos) {
        if (!hasNeighbour(blockPos)) {
            for (Direction side : Direction.values()) {
                BlockPos neighbour = blockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static boolean hasNeighbour(BlockPos blockPos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if (!mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }

}
