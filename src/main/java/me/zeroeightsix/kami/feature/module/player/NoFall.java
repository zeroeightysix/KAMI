package me.zeroeightsix.kami.feature.module.player;

import me.zeroeightsix.fiber.api.annotation.Settings;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * Created by 086 on 19/11/2017.
 */
@Module.Info(category = Module.Category.PLAYER, description = "Prevents fall damage", name = "NoFall")
@Settings(onlyAnnotated = true)
public class NoFall extends Module {

    /*@Setting(name = "Packet")
private boolean packet = false;
    @Setting(name = "Bucket")
private boolean bucket = true;
    @Setting(name = "Distance")
private int distance = 15;

    private long last = 0;

    @EventHandler
    public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof PlayerMoveC2SPacket && packet) {
            ((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(true);
        }
    });

    @Override
    public void onUpdate() {
        if (bucket && mc.player.fallDistance >= distance && !EntityUtil.isAboveWater(mc.player) && System.currentTimeMillis() - last > 100) {
            Vec3d posVec = mc.player.getPos();
            RayTraceResult result = mc.world.rayTraceBlocks(posVec, posVec.add(0, -5.33f, 0), true, true, false);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                EnumHand hand = EnumHand.MAIN_HAND;
                if (mc.player.getHeldItemOffhand().getItem() == Items.WATER_BUCKET) hand = EnumHand.OFF_HAND;
                else if (mc.player.getHeldItemMainhand().getItem() != Items.WATER_BUCKET) {
                    for (int i = 0; i < 9; i++)
                        if (mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
                            mc.player.inventory.selectedSlot = i;
                            mc.player.pitch = 90;
                            last = System.currentTimeMillis();
                            return;
                        }
                    return;
                }

                mc.player.pitch = 90;
                mc.interactionManager.processRightClick(mc.player, mc.world, hand);
                last = System.currentTimeMillis();
            }
        }
    }*/ //TODO
}
