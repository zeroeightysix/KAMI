package me.zeroeightsix.kami.util;

import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityUtil {

    public static boolean isPassive(Entity e){
        if (e instanceof WolfEntity && ((WolfEntity) e).getAngryAt() != null) return false;
        if (e instanceof AnimalEntity || e instanceof AmbientEntity || e instanceof SquidEntity) return true;
        return e instanceof IronGolemEntity && ((IronGolemEntity) e).getTarget() == null;
    }

    public static boolean isLiving(Entity e) {
        return e instanceof LivingEntity;
    }

    public static boolean isFakeLocalPlayer(Entity entity) {
        return entity != null && entity.getEntityId() == -100 && Wrapper.getPlayer() != entity;
    }

    public static boolean isMobAggressive(Entity entity) {
        if(entity instanceof ZombifiedPiglinEntity) {
            // angry = either game or we have set the anger cooldown
            if(((ZombifiedPiglinEntity) entity).isAngryAt(MinecraftClient.getInstance().player)) {
                return true;
            }
        } else if(entity instanceof WolfEntity) {
            /*return ((WolfEntity) entity).isAngry() &&
                !Wrapper.getPlayer().equals(((WolfEntity) entity).getOwner());*/
            return ((WolfEntity) entity).getAngryAt() == Wrapper.getPlayer().getUuid();
        } else if(entity instanceof EndermanEntity) {
            return ((EndermanEntity) entity).isAngry();
        }
        return isHostileMob(entity);
    }

    /**
     * If the mob by default wont attack the player, but will if the player attacks it
     */
    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof ZombifiedPiglinEntity ||
                entity instanceof WolfEntity ||
                entity instanceof EndermanEntity;
    }

    /**
     * If the mob is friendly (not aggressive)
     */
    public static boolean isFriendlyMob(Entity entity) {
        return (entity.getType().getSpawnGroup() == SpawnGroup.CREATURE && !EntityUtil.isNeutralMob(entity)) ||
                (entity.getType().getSpawnGroup() == SpawnGroup.AMBIENT ||
                entity instanceof VillagerEntity ||
                entity instanceof IronGolemEntity ||
                (isNeutralMob(entity) && !EntityUtil.isMobAggressive(entity)));
    }

    /**
     * If the mob is hostile
     */
    public static boolean isHostileMob(Entity entity) {
        return (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER && !EntityUtil.isNeutralMob(entity));
    }

    public static boolean isInWater(Entity entity) {
        if(entity == null) return false;

        double y = entity.getY() + 0.01;

        for(int x = MathHelper.floor(entity.getX()); x < MathHelper.ceil(entity.getX()); x++)
            for (int z = MathHelper.floor(entity.getZ()); z < MathHelper.ceil(entity.getZ()); z++) {
                BlockPos pos = new BlockPos(x, (int) y, z);

                if (Wrapper.getWorld().getBlockState(pos).getBlock() instanceof FluidBlock) return true;
            }

        return false;
    }

    public static boolean isDrivenByPlayer(Entity entityIn) {
        return Wrapper.getPlayer() != null && entityIn != null && entityIn.equals(Wrapper.getPlayer().getVehicle());
    }

    public static boolean isAboveWater(Entity entity) { return isAboveWater(entity, false); }
    public static boolean isAboveWater(Entity entity, boolean packet){
        if (entity == null) return false;

        double y = entity.getY() - (packet ? 0.03 : (EntityUtil.isPlayer(entity) ? 0.2 : 0.5)); // increasing this seems to flag more in NCP but needs to be increased so the player lands on solid water

        for(int x = MathHelper.floor(entity.getX()); x < MathHelper.ceil(entity.getX()); x++)
            for (int z = MathHelper.floor(entity.getZ()); z < MathHelper.ceil(entity.getZ()); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (Wrapper.getWorld().getBlockState(pos).getBlock() instanceof FluidBlock) return true;
            }

        return false;
    }

    public static double[] calculateLookAt(double px, double py, double pz, ClientPlayerEntity me) {
        double dirx = me.getX() - px;
        double diry = me.getY() - py;
        double dirz = me.getZ() - pz;

        double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        //to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]{yaw,pitch};
    }

    public static void updateVelocityX(Entity entity, double x) {
        Vec3d velocity = entity.getVelocity();
        entity.setVelocity(x, velocity.y, velocity.z);
    }

    public static void updateVelocityY(Entity entity, double y) {
        Vec3d velocity = entity.getVelocity();
        entity.setVelocity(velocity.x, y, velocity.z);
    }

    public static void updateVelocityZ(Entity entity, double z) {
        Vec3d velocity = entity.getVelocity();
        entity.setVelocity(velocity.x, velocity.y, z);
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof ClientPlayerEntity;
    }

    public static double getRelativeX(float yaw){
        return MathHelper.sin(-yaw * 0.017453292F);
    }

    public static double getRelativeZ(float yaw){
        return MathHelper.cos(yaw * 0.017453292F);
    }

}
