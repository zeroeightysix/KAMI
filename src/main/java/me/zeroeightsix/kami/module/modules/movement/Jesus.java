package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.KamiEvent;
import me.zeroeightsix.kami.event.events.AddCollisionBoxToListEvent;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket;
import me.zeroeightsix.kami.module.ModulePlay;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EmptyBlockView;

/**
 * Created by 086 on 11/12/2017.
 */
@ModulePlay.Info(name = "Jesus", description = "Allows you to walk on water", category = ModulePlay.Category.MOVEMENT)
public class Jesus extends ModulePlay {

    private static final Box WATER_WALK_AA = new Box(0.D, 0.D, 0.D, 1.D, 0.99D, 1.D);

    @Override
    public void onUpdate() {
        if (!ModuleManager.isModuleEnabled("Freecam")) {
            if (EntityUtil.isInWater(mc.player) && !mc.player.isSneaking()) {
                EntityUtil.updateVelocityY(mc.player, 0.1);
                if (mc.player.getVehicle() != null && !(mc.player.getVehicle() instanceof BoatEntity)) {
                    EntityUtil.updateVelocityY(mc.player.getVehicle(), 0.3);
                }
            }
        }
    }

    @EventHandler
    Listener<AddCollisionBoxToListEvent> addCollisionBoxToListEventListener = new Listener<>((event) -> {
        if (mc.player != null
                && (event.getBlock() instanceof FluidBlock)
                && (EntityUtil.isDrivenByPlayer(event.getEntity()) || event.getEntity()==mc.player)
                && !(event.getEntity() instanceof BoatEntity)
                && !mc.player.isSneaking()
                && mc.player.fallDistance < 3
                && !EntityUtil.isInWater(mc.player)
                && (EntityUtil.isAboveWater(mc.player, false) || EntityUtil.isAboveWater(mc.player.getVehicle(), false))
                && isAboveBlock(mc.player, event.getPos())) {
            Box axisalignedbb = WATER_WALK_AA.offset(event.getPos());
            if (event.getEntityBox().intersects(axisalignedbb)) event.getCollidingBoxes().add(axisalignedbb);
            event.cancel();
        }
    });

    @EventHandler
    Listener<PacketEvent.Send> packetEventSendListener = new Listener<>(event -> {
        if (event.getEra() == KamiEvent.Era.PRE) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket) {
                if (EntityUtil.isAboveWater(mc.player, true) && !EntityUtil.isInWater(mc.player) && !isAboveLand(mc.player)) {
                    int ticks = mc.player.age % 2;
                    if (ticks == 0) {
                        IPlayerMoveC2SPacket xyz = (IPlayerMoveC2SPacket) event.getPacket();
                        xyz.setY(xyz.getY() + 0.02D);
                    }
                }
            }
        }
    });

    @SuppressWarnings("deprecation")
    private static boolean isAboveLand(Entity entity){
        if(entity == null) return false;

        double y = entity.y - 0.01;

        for(int x = MathHelper.floor(entity.x); x < MathHelper.ceil(entity.x); x++)
            for (int z = MathHelper.floor(entity.z); z < MathHelper.ceil(entity.z); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (Wrapper.getWorld().getBlockState(pos).getBlock().isFullOpaque(Wrapper.getWorld().getBlockState(pos), EmptyBlockView.INSTANCE, pos)) return true;
            }

        return false;
    }

    private static boolean isAboveBlock(Entity entity, BlockPos pos) {
        return entity.y  >= pos.getY();
    }

}
