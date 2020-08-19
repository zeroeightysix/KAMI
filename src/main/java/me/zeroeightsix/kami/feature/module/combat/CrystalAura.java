package me.zeroeightsix.kami.feature.module.combat;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.PacketEvent;
import me.zeroeightsix.kami.event.RenderEvent;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket;
import me.zeroeightsix.kami.util.*;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.explosion.Explosion;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static me.zeroeightsix.kami.util.EntityUtil.calculateLookAt;

/**
 * Created by 086 on 28/12/2017.
 * Last Updated 29 June 2019 by hub
 */
@Module.Info(
        name = "CrystalAura",
        category = Module.Category.COMBAT
)
class CrystalAura extends Module {
    @Setting(name = "Auto Switch")
    private boolean autoSwitch = false;
    @Setting
    private boolean players = false;
    @Setting(name = "Mobs")
    private boolean mobs = false;
    @Setting(name = "Animals")
    private boolean animals = false;
    @Setting(name = "Place")
    private boolean place = false;
    @Setting(name = "Explode")
    private boolean explode = false;
    @Setting
    private double range = 4;
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

    public static CrystalAura INSTANCE;

    public CrystalAura() {
        INSTANCE = this;
    }

    @EventHandler
    private Listener<TickEvent.Client> clientListener = new Listener<>(client -> {
        EndCrystalEntity crystal = Stream.of(mc.world.getEntities())
                .filter(entity -> entity instanceof EndCrystalEntity)
                .map(entity -> (EndCrystalEntity) entity)
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
                        ItemStack stack = Wrapper.getPlayer().inventory.getStack(i);
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
                lookAtPacket(crystal.getX(), crystal.getY(), crystal.getZ(), mc.player);
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
                if (mc.player.inventory.getStack(l).getItem() == Items.END_CRYSTAL) {
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
            lookAtPacket(q.getX() + .5, q.getY() - .5, q.getZ() + .5, mc.player);
            RayTraceContext context = new RayTraceContext(new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ()), new Vec3d(q.getX() + .5, q.getY() - .5d, q.getZ() + .5), RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.rayTrace(context);
            Direction f;
            if (result == null || result.getSide() == null) {
                f = Direction.UP;
            } else {
                f = result.getSide();
            }
            // return after we did an autoswitch
            if (switchCooldown) {
                switchCooldown = false;
                return;
            }
            //mc.interactionManager.processRightClickBlock(mc.player, mc.world, q, f, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, result));
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
    });

    @EventHandler
    public Listener<RenderEvent.World> worldRenderListener = new Listener<>(worldEvent -> {
        if (render != null) {
            KamiTessellator.prepare(GL11.GL_QUADS);
            KamiTessellator.drawBox(render, 0x44ffffff, GeometryMasks.Quad.ALL);
            KamiTessellator.release();
            if (renderEnt != null) {
//                Vec3d p = EntityUtil.getInterpolatedRenderPos(renderEnt, mc.getTickDelta());
//                Tracers.drawLineFromPosToPos(render.getX() - ((IEntityRenderDispatcher) mc.getEntityRenderManager()).getRenderPosX() + .5d, render.getY() - ((IEntityRenderDispatcher) mc.getEntityRenderManager()).getRenderPosY() + 1, render.getZ() - ((IEntityRenderDispatcher) mc.getEntityRenderManager()).getRenderPosZ() + .5d, p.x, p.y, p.z, renderEnt.getEyeHeight(renderEnt.getPose()), 1, 1, 1, 1);
            }
        }
    });

    private void lookAtPacket(double px, double py, double pz, ClientPlayerEntity me) {
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
                || !mc.world.getNonSpectatingEntities(Entity.class, new Box(boost)).isEmpty()) {
            return false;
        }
        return true;
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.getX()), Math.floor(mc.player.getY()), Math.floor(mc.player.getZ()));
    }

    private List<BlockPos> findCrystalBlocks() {
        return new ArrayList<>(getSphere(getPlayerPos(), (float) range, (int) range, false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
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
        double distancedsize = Math.sqrt(entity.squaredDistanceTo(x, y, z)) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(x, y, z);
        // double blockDensity = (double) entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        // double v = (1.0D - distancedsize) * blockDensity;
        // float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finald = 1;
        // if (entity instanceof LivingEntity) {
        //     finald = getBlastReduction((LivingEntity) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, x, y, z, 6F, false, Explosion.DestructionType.NONE));
        // }
        return (float) finald;
    }

    public static float getBlastReduction(LivingEntity entity, float damage, Explosion explosion) {
        damage = DamageUtil.getDamageLeft(damage, (float) entity.getArmor(), (float) entity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            DamageSource damageSource = DamageSource.explosion(explosion);

            // damage = damage * (1.0F - f / 25.0F);

            // if (entity.isPotionEffective(Potion.byId("resistance"))) { // TODO: Is this the correct identifier?
            //     damage = damage - (damage / 4);
            // }

            damage = Math.max(damage - player.getAbsorptionAmount(), 0.0F);
            return damage;
        }
        return damage;
    }

    private static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(EndCrystalEntity crystal, Entity entity) {
        return calculateDamage(crystal.getX(), crystal.getY(), crystal.getZ(), entity);
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
                ((IPlayerMoveC2SPacket) packet).setYaw((float) yaw);
                ((IPlayerMoveC2SPacket) packet).setPitch((float) pitch);
            }
        }
    });

    @Override
    public void onDisable() {
        render = null;
        renderEnt = null;
        resetRotation();
    }

}
