package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.ModulePlay;

/**
 * Created by 086 on 19/11/2017.
 */
@ModulePlay.Info(category = ModulePlay.Category.PLAYER, description = "Prevents fall damage", name = "NoFall")
public class NoFall extends ModulePlay {

    /*private Setting<Boolean> packet = register(Settings.b("Packet", false));
    private Setting<Boolean> bucket = register(Settings.b("Bucket", true));
    private Setting<Integer> distance = register(Settings.i("Distance", 15));

    private long last = 0;

    @EventHandler
    public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof PlayerMoveC2SPacket && packet.getValue()) {
            ((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(true);
        }
    });

    @Override
    public void onUpdate() {
        if (bucket.getValue() && mc.player.fallDistance >= distance.getValue() && !EntityUtil.isAboveWater(mc.player) && System.currentTimeMillis() - last > 100) {
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
