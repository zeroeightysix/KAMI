package me.zeroeightsix.kami.util;

import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

import java.util.List;

/**
 * Created by Gebruiker on 27/02/2017.
 */
public class TrajectoryCalculator {

    /**
     * Check if the thePlayer is holding an item that can be thrown/shot.
     *
     * @param entity an entity
     * @return true if can shoot/throw, false otherwise
     */
    public static ThrowingType getThrowType(LivingEntity entity) {
        // Check if we're holding an item first
        Hand activeHand = entity.getActiveHand();
        ItemStack activeItem = entity.getStackInHand(activeHand); // getActiveItem might return null when not in use?
        // so i use this method, as active hand is always main or offhand, never null

        if (activeItem.isEmpty()) {
            return ThrowingType.NONE;
        }

        Item item = activeItem.getItem();
        // The potion is kind of special so we do it's own check
        if (item instanceof PotionItem) {
            // Check if it's a splashable potion
            if (item instanceof SplashPotionItem){
                return ThrowingType.POTION;
            }
        } else if (item instanceof BowItem && entity.isUsingItem()) {
            return ThrowingType.BOW;
        } else if (item instanceof ExperienceBottleItem) {
            return ThrowingType.EXPERIENCE;
        } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) {
            return ThrowingType.NORMAL;
        }

        // Unknown type
        return ThrowingType.NONE;
    }

    public enum ThrowingType {
        NONE, BOW, EXPERIENCE, POTION, NORMAL
    }

    /**
     * A class used to mimic the flight of an entity.  Actual
     * implementation resides in multiple classes but the parent of all
     * of them is {@link net.minecraft.entity.projectile.Projectile}
     */
    public static final class FlightPath {
        private LivingEntity shooter;
        public Vec3d position;
        private Vec3d motion;
        private float yaw;
        private float pitch;
        private Box boundingBox;
        private boolean collided;
        private HitResult target;
        private ThrowingType throwingType;

        public FlightPath(LivingEntity entityLivingBase, ThrowingType throwingType) {
            this.shooter = entityLivingBase;
            this.throwingType = throwingType;

            Vec3d ipos = interpolate(shooter);

            // Set the starting angles of the entity
            Vec3d renderPos = Wrapper.getRenderPosition();
            this.setLocationAndAngles(ipos.x + renderPos.getX(), ipos.y + this.shooter.getEyeHeight(this.shooter.getPose()) + renderPos.getY(), ipos.z + renderPos.getZ(),
                    this.shooter.yaw, this.shooter.pitch);
            Vec3d startingOffset = new Vec3d(MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * 0.16F, 0.1d,
                    MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * 0.16F);
            this.position = this.position.subtract(startingOffset);
            // Update the entity's bounding box
            this.setPosition(this.position);

            // Set the entity's motion based on the shooter's rotations
            this.motion = new Vec3d(-MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI),
                    -MathHelper.sin(this.pitch / 180.0F * (float) Math.PI),
                    MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI));
            this.setThrowableHeading(this.motion, this.getInitialVelocity());
        }

        /**
         * Update the entity's data in the world.
         */
        public void onUpdate() {
            // Get the predicted positions in the world
            Vec3d prediction = this.position.add(this.motion);
            // Check if we've collided with a block in the same time
            RayTraceContext context = new RayTraceContext(this.position, prediction, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, Wrapper.getPlayer());
            BlockHitResult blockCollision = this.shooter.getEntityWorld().rayTrace(context);
            // Check if we got a block collision
            if (blockCollision != null) {
                prediction = blockCollision.getPos();
            }

            // Check entity collision
            this.onCollideWithEntity(prediction, blockCollision);

            // Check if we had a collision
            if (this.target != null) {
                this.collided = true;
                // Update position
                this.setPosition(this.target.getPos());
                return;
            }

            // Sanity check to see if we've gone below the world (if we have we will never collide)
            if (this.position.y <= 0.0d) {
                // Force this to true even though we haven't collided with anything
                this.collided = true;
                return;
            }

            // Update the entity's position based on velocity
            this.position = this.position.add(this.motion);
            float motionModifier = 0.99F;
            // Check if our path will collide with water
            if (this.shooter.getEntityWorld().containsBlockWithMaterial(this.boundingBox, Material.WATER)) {
                motionModifier = this.throwingType == ThrowingType.BOW ? 0.6F : 0.8F;
            }

            // Slowly decay the velocity of the path
            this.motion = mult(this.motion, motionModifier);
            // Drop the motionY by the constant gravity
            this.motion = this.motion.subtract(0.0d, this.getGravityVelocity(), 0.0d);
            // Update the position and bounding box
            this.setPosition(this.position);
        }

        /**
         * Check if our path collides with an entity.
         *
         * @param prediction the predicted position
         * @param blockCollision block collision if we had one
         */
        private void onCollideWithEntity(Vec3d prediction, BlockHitResult blockCollision) {
            Entity collidingEntity = null;
            double currentDistance = 0.0d;
            // Get all possible collision entities disregarding the local thePlayer
            List<Entity> collisionEntities = this.shooter.world.getEntities(this.shooter, this.boundingBox.expand(this.motion.x, this.motion.y, this.motion.z).expand(1.0D, 1.0D, 1.0D));;

            if (collisionEntities.size() > 0) collidingEntity = collisionEntities.get(0);

            // Check if we had an entity
            if (collidingEntity != null) {
                // Set our target to the result
                this.target = new EntityHitResult(collidingEntity);
            } else {
                // Fallback to the block collision
                this.target = blockCollision;
            }
        }

        /**
         * Return the initial velocity of the entity at it's exact starting
         * moment in flight.
         *
         * @return entity velocity in flight
         */
        private float getInitialVelocity() {
            ItemStack stack = this.shooter.getActiveItem();
            Item item = stack.getItem();
            switch (this.throwingType) {
                case BOW:
                    // A local instance of the bow we are holding
                    BowItem bow = (BowItem) item;
                    // Check how long we've been using the bow
                    int useDuration = this.shooter.getItemUseTimeLeft();
                    float velocity = (float) useDuration / 20.0F;
                    velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
                    if (velocity > 1.0F) {
                        velocity = 1.0F;
                    }

                    // When the arrow is spawned inside of ItemBow, they multiply it by 2
                    return (velocity * 2.0f) * 1.5f;
                case POTION:
                    return 0.5F;
                case EXPERIENCE:
                    return 0.7F;
                case NORMAL:
                    return 1.5f;
            }
            // The standard gravity
            return 1.5f;
        }

        /**
         * Get the constant gravity of the item in use.
         *
         * @return gravity relating to item
         */
        private float getGravityVelocity() {
            switch (this.throwingType) {
                case BOW:
                case POTION:
                    return 0.05f;
                case EXPERIENCE:
                    return 0.07f;
                case NORMAL:
                    return 0.03f;
            }

            // The standard gravity
            return 0.03f;
        }

        /**
         * Set the position and rotation of the entity in the world.
         *
         * @param x x position in world
         * @param y y position in world
         * @param z z position in world
         * @param yaw yaw rotation axis
         * @param pitch pitch rotation axis
         */
        private void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
            this.position = new Vec3d(x, y, z);
            this.yaw = yaw;
            this.pitch = pitch;
        }

        /**
         * Sets the x,y,z of the entity from the given parameters. Also seems to set
         * up a bounding box.
         *
         * @param position position in world
         */
        private void setPosition(Vec3d position) {
            this.position = new Vec3d(position.x, position.y, position.z);
            // Usually this is this.width / 2.0f but throwables change
            double entitySize = (this.throwingType == ThrowingType.BOW ? 0.5d : 0.25d) / 2.0d;
            // Update the path's current bounding box
            this.boundingBox = new Box(position.x - entitySize,
                    position.y - entitySize,
                    position.z - entitySize,
                    position.x + entitySize,
                    position.y + entitySize,
                    position.z + entitySize);
        }

        /**
         * Set the entity's velocity and position in the world.
         *
         * @param motion velocity in world
         * @param velocity starting velocity
         */
        private void setThrowableHeading(Vec3d motion, float velocity) {
            // Divide the current motion by the length of the vector
            this.motion = div(motion, (float) motion.length());
            // Multiply by the velocity
            this.motion = mult(this.motion, velocity);
        }

        /**
         * Check if the path has collided with an object.
         *
         * @return path collides with ground
         */
        public boolean isCollided() {
            return collided;
        }

        /**
         * Get the target we've collided with if it exists.
         *
         * @return moving object target
         */
        public HitResult getCollidingTarget() {
            return target;
        }
    }

    public static Vec3d interpolate(Entity entity) {
        Vec3d now = entity.getPos();
        Vec3d then = new Vec3d(entity.prevX, entity.prevY, entity.prevZ);
        Vec3d diff = now.subtract(then);
        return now.add(diff.multiply(Wrapper.getMinecraft().getTickDelta())).subtract(Wrapper.getRenderPosition());
    }

    public static double interpolate(double now, double then) {
        return then + (now - then) * Wrapper.getMinecraft().getTickDelta();
    }

    public static Vec3d mult(Vec3d factor, float multiplier) {
        return new Vec3d(factor.x * multiplier, factor.y * multiplier, factor.z * multiplier);
    }

    public static Vec3d div(Vec3d factor, float divisor) {
        return new Vec3d(factor.x / divisor, factor.y / divisor, factor.z / divisor);
    }
}
