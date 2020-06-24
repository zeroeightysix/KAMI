package me.zeroeightsix.kami.feature.module.combat

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings
import me.zeroeightsix.kami.feature.module.Module

/**
 * Created by 086 on 28/12/2017.
 * Last Updated 29 June 2019 by hub
 */
@Module.Info(
    name = "CrystalAura",
    category = Module.Category.COMBAT
)
object CrystalAura : Module() { /*private Setting<Boolean> autoSwitch = register(Settings.b("Auto Switch"));
    private Setting<Boolean> players = register(Settings.b("Players"));
    @Setting(name = "Mobs")
private boolean mobs = false;
    @Setting(name = "Animals")
private boolean animals = false;
    @Setting(name = "Place")
private boolean place = false;
    @Setting(name = "Explode")
private boolean explode = false;
    private Setting<Double>  range = register(Settings.d("Range", 4));
    @Setting(name = "Anti Weakness")
private boolean antiWeakness = false;

    private BlockPos render;
    private Entity renderEnt;
    private long systemTime = -1;
    private static boolean togglePitch = false;
	// we need this cooldown to not place from old hotbar slot, before we have switched to crystals
    private boolean switchCooldown = false;
    private boolean isAttacking = false;
    private int oldSlot = -1;
    private int newSlot;

    @Override
    public void onUpdate() {
        EnderCrystalEntity crystal = Stream.of(mc.world.getEntities())
                .filter(entity -> entity instanceof EnderCrystalEntity)
                .map(entity -> (EnderCrystalEntity) entity)
                .min(Comparator.comparing(c -> mc.player.distanceTo(c)))
                .orElse(null);
        if (explode && crystal != null && mc.player.distanceTo(crystal) <= range) {
            //Added delay to stop ncp from flagging "hitting too fast"
            if (((System.nanoTime() / 1000000) - systemTime) >= 250) {
                if (antiWeakness && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                    if (!isAttacking) {
                        // save initial player hand
                        oldSlot = Wrapper.getPlayer().inventory.selectedSlot;
                        isAttacking = true;
                    }
                    // search for sword and tools in hotbar
                    newSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = Wrapper.getPlayer().inventory.getInvStack(i);
                        if (stack == ItemStack.EMPTY) {
                            continue;
                        }
                        if ((stack.getItem() instanceof SwordItem)) {
                            newSlot = i;
                            break;
                        }
                        if ((stack.getItem() instanceof ToolItem)) {
                            newSlot = i;
                            break;
                        }
                    }
                    // check if any swords or tools were found
                    if (newSlot != -1) {
                        Wrapper.getPlayer().inventory.selectedSlot = newSlot;
                        switchCooldown = true;
                    }
                }
                lookAtPacket(crystal.x, crystal.y, crystal.z, mc.player);
                mc.interactionManager.attackEntity(mc.player, crystal);
                mc.player.swingHand(Hand.MAIN_HAND);
                systemTime = System.nanoTime() / 1000000;
            }
            return;
        } else {
            resetRotation();
			if (oldSlot != -1) {
                Wrapper.getPlayer().inventory.selectedSlot = oldSlot;
                oldSlot = -1;
			}
            isAttacking = false;
        }

        int crystalSlot = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ? mc.player.inventory.selectedSlot : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getInvStack(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }

        boolean offhand = false;
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
            offhand = true;
        } else if (crystalSlot == -1) {
            return;
        }

        List<BlockPos> blocks = findCrystalBlocks();
        List<Entity> entities = new ArrayList<>();
        if (players) {
            entities.addAll(mc.world.getPlayers().stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName().getString())).collect(Collectors.toList()));
        }
        entities.addAll(StreamSupport.stream(mc.world.getEntities().spliterator(), false).filter(entity -> EntityUtil.isLiving(entity) && (EntityUtil.isPassive(entity) ? animals : mobs)).collect(Collectors.toList()));

        BlockPos q = null;
        double damage = .5;
        for (Entity entity : entities) {
            if (entity == mc.player || ((LivingEntity) entity).getHealth() <= 0) {
                continue;
            }
            for (BlockPos blockPos : blocks) {
                double b = entity.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                if (b >= 169) {
                    continue; // If this block if further than 13 (3.6^2, less calc) blocks, ignore it. It'll take no or very little damage
                }
                double d = calculateDamage(blockPos.getX() + .5, blockPos.getY() + 1, blockPos.getZ() + .5, entity);
                if (d > damage) {
                    double self = calculateDamage(blockPos.getX() + .5, blockPos.getY() + 1, blockPos.getZ() + .5, mc.player);
                    // If this deals more damage to ourselves than it does to our target, continue. This is only ignored if the crystal is sure to kill our target but not us.
                    // Also continue if our crystal is going to hurt us.. alot
                    if ((self > d && !(d < ((LivingEntity) entity).getHealth())) || self - .5 > mc.player.getHealth()) {
                        continue;
                    }
                    damage = d;
                    q = blockPos;
                    renderEnt = entity;
                }
            }
        }
        if (damage == .5) {
            render = null;
            renderEnt = null;
            resetRotation();
            return;
        }
        render = q;

        if (place) {
            if (!offhand && mc.player.inventory.selectedSlot != crystalSlot) {
                if (autoSwitch) {
                    mc.player.inventory.selectedSlot = crystalSlot;
                    resetRotation();
                    switchCooldown = true;
                }
                return;
            }
            lookAtPacket(q.x + .5, q.y - .5, q.z + .5, mc.player);
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.x, mc.player.y + mc.player.getEyeHeight(), mc.player.z), new Vec3d(q.x + .5, q.y - .5d, q.z + .5));
            Direction f;
            if (result == null || result.sideHit == null) {
                f = Direction.UP;
            } else {
                f = result.sideHit;
            }
            // return after we did an autoswitch
            if (switchCooldown) {
                switchCooldown = false;
                return;
            }
            //mc.interactionManager.processRightClickBlock(mc.player, mc.world, q, f, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new PlayerMoveC2SPacketTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        }
        //this sends a constant packet flow for default packets
        if (isSpoofingAngles) {
            if (togglePitch) {
                mc.player.pitch += 0.0004;
                togglePitch = false;
            } else {
                mc.player.pitch -= 0.0004;
                togglePitch = true;
            }
        }

    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (render != null) {
            KamiTessellator.prepare(GL11.GL_QUADS);
            KamiTessellator.drawBox(render, 0x44ffffff, GeometryMasks.Quad.ALL);
            KamiTessellator.release();
            if (renderEnt != null) {
                Vec3d p = EntityUtil.getInterpolatedRenderPos(renderEnt, mc.getRenderPartialTicks());
                Tracers.drawLineFromPosToPos(render.x - mc.getEntityRenderManager().renderPosX + .5d, render.y - mc.getEntityRenderManager().renderPosY + 1, render.z - mc.getEntityRenderManager().renderPosZ + .5d, p.x, p.y, p.z, renderEnt.getEyeHeight(), 1, 1, 1, 1);
            }
        }
    }

    private void lookAtPacket(double px, double py, double pz, PlayerEntity me) {
        double[] v = calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1]);
    }

    private boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        if ((mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK
                && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN)
                || mc.world.getBlockState(boost).getBlock() != Blocks.AIR
                || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR
                || !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()) {
            return false;
        }
        return true;
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.x), Math.floor(mc.player.y), Math.floor(mc.player.z));
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(getSphere(getPlayerPos(), range.floatValue(), range.intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static float calculateDamage(double x, double y, double z, Entity entity) {
        float doubleExplosionSize = 6.0F * 2.0F;
        double distancedsize = entity.getDistance(x, y, z) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(x, y, z);
        double blockDensity = (double) entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedsize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finald = 1;
        /*if (entity instanceof EntityLivingBase)
            finald = getBlastReduction((EntityLivingBase) entity,getDamageMultiplied(damage));*/
/*
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, x, y, z, 6F, false, true));
        }
        return (float) finald;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp(k, 0.0F, 20.0F);
            damage = damage * (1.0F - f / 25.0F);

            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage = damage - (damage / 4);
            }

            damage = Math.max(damage - ep.getAbsorptionAmount(), 0.0F);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(EnderCrystalEntity crystal, Entity entity) {
        return calculateDamage(crystal.x, crystal.y, crystal.z, entity);
    }

    //Better Rotation Spoofing System:

    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;

    //this modifies packets being sent so no extra ones are made. NCP used to flag with "too many packets"
    private static void setYawAndPitch(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
        isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (isSpoofingAngles) {
            yaw = mc.player.yaw;
            pitch = mc.player.pitch;
            isSpoofingAngles = false;
        }
    }


    @EventHandler
    private Listener<PacketEvent.Send> packetListener = new Listener<>(event -> {
        Packet packet = event.getPacket();
        if (packet instanceof PlayerMoveC2SPacket) {
            if (isSpoofingAngles) {
                ((PlayerMoveC2SPacket) packet).yaw = (float) yaw;
                ((PlayerMoveC2SPacket) packet).pitch = (float) pitch;
            }
        }
    });

    @Override
    public void onDisable() {
        render = null;
        renderEnt = null;
        resetRotation();
    }*/*/
//TODO
}