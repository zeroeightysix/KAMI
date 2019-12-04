package me.zeroeightsix.kami.util;

import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombiePigmanEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityUtil {

    public static boolean isPassive(Entity e){
        if (e instanceof WolfEntity && ((WolfEntity) e).isAngry()) return false;
        if (e instanceof AnimalEntity || e instanceof AmbientEntity || e instanceof SquidEntity) return true;
        return e instanceof IronGolemEntity && ((IronGolemEntity) e).getTarget() == null;
    }

    public static boolean isLiving(Entity e) {
        return e instanceof LivingEntity;
    }

    public static boolean isFakeLocalPlayer(Entity entity) {
        return entity != null && entity.getEntityId() == -100 && Wrapper.getPlayer() != entity;
    }

    /**
     * Find the entities interpolated amount
     */
    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return entity.getPos().subtract(entity.prevX, entity.prevY, entity.prevZ).multiply(x, y, z);
    }
    public static Vec3d getInterpolatedAmount(Entity entity, Vec3d vec) {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
    }
    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static boolean isMobAggressive(Entity entity) {
        if(entity instanceof ZombiePigmanEntity) {
            // angry = either game or we have set the anger cooldown
            if(((ZombiePigmanEntity) entity).isAngryAt(MinecraftClient.getInstance().player)) {
                return true;
            }
        } else if(entity instanceof WolfEntity) {
            return ((WolfEntity) entity).isAngry() &&
                    !Wrapper.getPlayer().equals(((WolfEntity) entity).getOwner());
        } else if(entity instanceof EndermanEntity) {
            return ((EndermanEntity) entity).isAngry();
        }
        return isHostileMob(entity);
    }

    /**
     * If the mob by default wont attack the player, but will if the player attacks it
     */
    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof ZombiePigmanEntity ||
                entity instanceof WolfEntity ||
                entity instanceof EndermanEntity;
    }

    /**
     * If the mob is friendly (not aggressive)
     */
    public static boolean isFriendlyMob(Entity entity) {
        return (entity.getType().getCategory() == EntityCategory.CREATURE && !EntityUtil.isNeutralMob(entity)) ||
                (entity.getType().getCategory() == EntityCategory.AMBIENT ||
                entity instanceof VillagerEntity ||
                entity instanceof IronGolemEntity ||
                (isNeutralMob(entity) && !EntityUtil.isMobAggressive(entity)));
    }

    /**
     * If the mob is hostile
     */
    public static boolean isHostileMob(Entity entity) {
        return (entity.getType().getCategory() == EntityCategory.MONSTER && !EntityUtil.isNeutralMob(entity));
    }

    /**
     * Find the entities interpolated position
     */
    public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
        return new Vec3d(entity.prevX, entity.prevY, entity.prevZ).add(getInterpolatedAmount(entity, ticks));
    }

    public static Vec3d getInterpolatedRenderPos(Entity entity, float ticks) {
        Vec3d renderPos = Wrapper.getRenderPosition();
        return getInterpolatedPos(entity, ticks).subtract(renderPos);
    }

    public static boolean isInWater(Entity entity) {
        if(entity == null) return false;

        double y = entity.y + 0.01;

        for(int x = MathHelper.floor(entity.x); x < MathHelper.ceil(entity.x); x++)
            for (int z = MathHelper.floor(entity.z); z < MathHelper.ceil(entity.z); z++) {
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

        double y = entity.y - (packet ? 0.03 : (EntityUtil.isPlayer(entity) ? 0.2 : 0.5)); // increasing this seems to flag more in NCP but needs to be increased so the player lands on solid water

        for(int x = MathHelper.floor(entity.x); x < MathHelper.ceil(entity.x); x++)
            for (int z = MathHelper.floor(entity.z); z < MathHelper.ceil(entity.z); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (Wrapper.getWorld().getBlockState(pos).getBlock() instanceof FluidBlock) return true;
            }

        return false;
    }

    public static double[] calculateLookAt(double px, double py, double pz, ClientPlayerEntity me) {
        double dirx = me.x - px;
        double diry = me.y - py;
        double dirz = me.z - pz;

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
