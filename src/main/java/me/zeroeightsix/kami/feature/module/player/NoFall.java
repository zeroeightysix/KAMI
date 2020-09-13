package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.PacketEvent;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@Module.Info(category = Module.Category.PLAYER, description = "Prevents fall damage", name = "NoFall")
public class NoFall extends Module {

    @Setting
    private boolean packet = false;
    @Setting
    private boolean bucket = true;
    @Setting
    private int distance = 15;

    private long last = 0;

    @EventHandler
    public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof PlayerMoveC2SPacket && packet) {
            ((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(true);
        }
    });

    @EventHandler
    public Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (bucket && mc.player.fallDistance >= distance && !EntityUtil.isAboveWater(mc.player) && System.currentTimeMillis() - last > 10) {
            Vec3d posVec = mc.player.getPos();
            RaycastContext context = new RaycastContext(posVec, posVec.add(0, -distance, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(context);
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                Hand hand = Hand.MAIN_HAND;
                if (mc.player.getOffHandStack().getItem() == Items.WATER_BUCKET) hand = Hand.OFF_HAND;
                else if (mc.player.getMainHandStack().getItem() != Items.WATER_BUCKET) {
                    for (int i = 0; i < 9; i++)
                        if (mc.player.inventory.getStack(i).getItem() == Items.WATER_BUCKET) {
                            mc.player.inventory.selectedSlot = i;
                            mc.player.pitch = 90;
                            last = System.currentTimeMillis();
                            return;
                        }
                    return;
                }

                mc.player.pitch = 90;
                mc.interactionManager.interactItem(mc.player, mc.world, hand);
                last = System.currentTimeMillis();
            }
        }
    });
}
