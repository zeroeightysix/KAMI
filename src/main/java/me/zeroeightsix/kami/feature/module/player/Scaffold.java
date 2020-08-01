package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Freecam;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.*;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

@Module.Info(name = "Scaffold", category = Module.Category.PLAYER)
public class Scaffold extends Module {

    private List<Block> blackList = Arrays.asList(Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST);

    @Setting(name = "Ticks")
    private @Setting.Constrain.Range(min = 0, max = 60) int future = 2;

    private boolean hasNeighbour(BlockPos blockPos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if(!Wrapper.getWorld().getBlockState(neighbour).getMaterial().isReplaceable())
                return true;
        }
        return false;
    }

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (isDisabled() || Freecam.INSTANCE.isEnabled()) return;
        Vec3d vec3d = EntityUtil.getInterpolatedPos(mc.player, future);
        BlockPos blockPos = new BlockPos(vec3d).down();
        BlockPos belowBlockPos = blockPos.down();

        // check if block is already placed
        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable())
            return;

        // search blocks in hotbar
        int newSlot = -1;
        for(int i = 0; i < 9; i++)
        {
            // filter out non-block items
            ItemStack stack = mc.player.inventory.getStack(i);

            if(stack == ItemStack.EMPTY || !(stack.getItem() instanceof BlockItem)) {
                continue;
            }
            Block block = ((BlockItem) stack.getItem()).getBlock();
            if (blackList.contains(block) || block instanceof BlockWithEntity) {
                continue;
            }

            // filter out non-solid blocks
            if(!Block.getBlockFromItem(stack.getItem()).getDefaultState()
                    .isOpaque())
                continue;

            // don't use falling blocks if it'd fall
            if (((BlockItem) stack.getItem()).getBlock() instanceof FallingBlock) {
                if (Wrapper.getWorld().getBlockState(belowBlockPos).getMaterial().isReplaceable()) continue;
            }

            newSlot = i;
            break;
        }

        // check if any blocks were found
        if(newSlot == -1)
            return;

        // set slot
        int oldSlot = Wrapper.getPlayer().inventory.selectedSlot;
        Wrapper.getPlayer().inventory.selectedSlot = newSlot;

        // check if we don't have a block adjacent to blockpos
        A: if (!hasNeighbour(blockPos)) {
            // find air adjacent to blockpos that does have a block adjacent to it, let's fill this first as to form a bridge between the player and the original blockpos. necessary if the player is going diagonal.
            for (Direction side : Direction.values()) {
                BlockPos neighbour = blockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    blockPos = neighbour;
                    break A;
                }
            }
            return;
        }

        // place block
        placeBlockScaffold(blockPos);

        // reset slot
        Wrapper.getPlayer().inventory.selectedSlot = oldSlot;
    });

    public static boolean placeBlockScaffold(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().getX(),
                Wrapper.getPlayer().getY() + Wrapper.getPlayer().getEyeHeight(Wrapper.getPlayer().getPose()),
                Wrapper.getPlayer().getZ());

        for(Direction side : Direction.values())
        {
            BlockPos neighbor = pos.offset(side);
            Direction side2 = side.getOpposite();

            // check if side is visible (facing away from player)
            if(eyesPos.squaredDistanceTo(
                    new Vec3d(new Vector3f(pos.getX(),pos.getY(),pos.getZ())).add(0.5, 0.5, 0.5)) >= eyesPos
                    .squaredDistanceTo(
                            new Vec3d(new Vector3f(neighbor.getX(),neighbor.getY(),neighbor.getZ())).add(0.5, 0.5, 0.5)))
                continue;

            // check if neighbor can be right clicked
            if(!canBeClicked(neighbor))
                continue;

            Vec3d hitVec = new Vec3d(new Vector3f(neighbor.getX(),neighbor.getY(),neighbor.getZ())).add(0.5, 0.5, 0.5)
                    .add(new Vec3d(new Vector3f(side2.getVector().getX(),side2.getVector().getY(),side2.getVector().getZ())).multiply(0.5));

            // check if hitVec is within range (4.25 blocks)
            if(eyesPos.squaredDistanceTo(hitVec) > 18.0625)
                continue;

            // place block
            faceVectorPacketInstant(hitVec);
            processRightClickBlock(neighbor, side2, hitVec);
            Wrapper.getPlayer().swingHand(Hand.MAIN_HAND);
            ((IMinecraftClient) mc).setItemUseCooldown(4);

            return true;
        }

        return false;
    }

    public static void processRightClickBlock(BlockPos pos, Direction side,
                                              Vec3d hitVec)
    {
        mc.interactionManager.interactBlock(Wrapper.getPlayer(),
                mc.world, Hand.MAIN_HAND, new BlockHitResult(hitVec, side, pos, false));
    }

    public static BlockState getState(BlockPos pos)
    {
        return Wrapper.getWorld().getBlockState(pos);
    }

    public static Block getBlock(BlockPos pos)
    {
        return getState(pos).getBlock();
    }

    public static boolean canBeClicked(BlockPos pos)
    {
        //return getBlock(pos).canCollideCheck(getState(pos), false);
        return true; // TODO
    }

    public static void faceVectorPacketInstant(Vec3d vec)
    {
        float[] rotations = getNeededRotations2(vec);

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookOnly(rotations[0],
                rotations[1], Wrapper.getPlayer().isOnGround()));
    }

    private static float[] getNeededRotations2(Vec3d vec)
    {
        Vec3d eyesPos = getEyesPos();

        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]{
                Wrapper.getPlayer().yaw
                        + MathHelper.wrapDegrees(yaw - Wrapper.getPlayer().yaw),
                Wrapper.getPlayer().pitch + MathHelper
                        .wrapDegrees(pitch - Wrapper.getPlayer().pitch)};
    }

    public static Vec3d getEyesPos()
    {
        return new Vec3d(Wrapper.getPlayer().getX(),
                Wrapper.getPlayer().getY() + Wrapper.getPlayer().getEyeHeight(mc.player.getPose()),
                Wrapper.getPlayer().getZ());
    }

}

