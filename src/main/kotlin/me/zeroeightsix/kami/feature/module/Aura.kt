package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.TickEvent
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.LagCompensator
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RayTraceContext

@Module.Info(
    name = "Aura",
    category = Module.Category.COMBAT,
    description = "Hits entities around you"
)
object Aura : Module() {

    @Setting(name = "Players")
    private var attackPlayers = true

    @Setting(name = "Mobs")
    private var attackMobs = false

    @Setting(name = "Animals")
    private var attackAnimals = false

    @Setting(name = "Hit Range")
    private var hitRange: Double = 5.5

    @Setting(name = "Ignore Walls")
    private var ignoreWalls = true

    @Setting(name = "Mode")
    private var waitMode = WaitMode.DYNAMIC

    @Setting(name = "Tick Delay")
    @SettingVisibility.Method("ifModeStatic")
    private var waitTick: @Setting.Constrain.Range(
        min = 0.0,
        max = 3.0, /* TODO: Remove when kotlin bug fixed */
        step = java.lang.Double.MIN_VALUE
    ) Int = 3

    @Setting(name = "32k Switch")
    private var switchTo32k = true

    @Setting(name = "32k Only")
    private var onlyUse32k = false
    private var waitCounter = 0

    fun ifModeStatic() = waitMode == WaitMode.STATIC

    @EventHandler
    private val updateListener =
        Listener(EventHook<TickEvent.Client.InGame> {
            if (!mc.player?.isAlive!!) {
                return@EventHook
            }
            val shield =
                mc.player!!.offHandStack.item == Items.SHIELD && mc.player!!.activeHand == Hand.OFF_HAND
            if (mc.player!!.isUsingItem && !shield) {
                return@EventHook
            }
            if (waitMode == WaitMode.DYNAMIC) {
                if (mc.player!!.getAttackCooldownProgress(lagComp) < 1) { // TODO: Is the right function?
                    return@EventHook
                } else if (mc.player!!.age % 2 != 0) {
                    return@EventHook
                }
            }
            if (waitMode == WaitMode.STATIC && waitTick > 0) {
                waitCounter = if (waitCounter < waitTick) {
                    waitCounter++
                    return@EventHook
                } else {
                    0
                }
            }
            for (target in mc.world?.entities!!) {
                if (!EntityUtil.isLiving(target)) {
                    continue
                }
                if (target === mc.player) {
                    continue
                }
                if (mc.player!!.distanceTo(target) > hitRange) {
                    continue
                }
                if ((target as LivingEntity).health <= 0) {
                    continue
                }
                if (waitMode == WaitMode.DYNAMIC && target.hurtTime != 0) {
                    continue
                }
                if (!ignoreWalls && !mc.player!!.canSee(target) && !canEntityFeetBeSeen(
                        target
                    )
                ) {
                    continue  // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed
                }
                if (attackPlayers && target is PlayerEntity && !Friends.isFriend(target.getName().string)) {
                    attack(target)
                    return@EventHook
                } else {
                    if (if (EntityUtil.isPassive(target)) attackAnimals else EntityUtil.isMobAggressive(
                            target
                        ) && attackMobs
                    ) {
                        // We want to skip this if switchTo32k is true,
                        // because it only accounts for tools and weapons.
                        // Maybe someone could refactor this later? :3
                        if (!switchTo32k && AutoTool.enabled) {
                            AutoTool.equipBestWeapon()
                        }
                        attack(target)
                        return@EventHook
                    }
                }
            }
        })

    private fun checkSharpness(stack: ItemStack): Boolean {
        val tag = stack.tag ?: return false
        val enchantments = stack.enchantments
        for (i in enchantments.indices) {
            val enchantment = enchantments.getCompound(i)
            if (enchantment.getInt("id") == 16) { // id of sharpness
                val lvl = enchantment.getInt("lvl")
                if (lvl >= 34) return true
                break // we've already found sharpness; no other enchant will match id == 16
            }
        }
        return false
    }

    private fun attack(e: Entity) {
        var holding32k = false
        if (mc.player?.activeItem?.let { checkSharpness(it) }!!) {
            holding32k = true
        }
        if (switchTo32k && !holding32k) {
            var newSlot = -1
            for (i in 0..8) {
                val stack = mc.player?.inventory?.getStack(i)
                if (stack == ItemStack.EMPTY) {
                    continue
                }
                if (stack?.let { checkSharpness(it) }!!) {
                    newSlot = i
                    break
                }
            }
            if (newSlot != -1) {
                mc.player?.inventory?.selectedSlot = newSlot
                holding32k = true
            }
        }
        if (onlyUse32k && !holding32k) {
            return
        }
        mc.interactionManager?.attackEntity(
            mc.player,
            e
        )
        mc.player?.swingHand(Hand.MAIN_HAND)
    }

    private val lagComp: Float
        private get() = if (waitMode == WaitMode.DYNAMIC) {
            -(20 - LagCompensator.INSTANCE.tickRate)
        } else 0.0f

    private fun canEntityFeetBeSeen(entityIn: Entity): Boolean {
        val context = RayTraceContext(
            mc.player?.getEyeHeight(mc.player!!.pose)?.toDouble()?.let {
                mc.player?.pos?.add(
                    0.0,
                    it,
                    0.0
                )
            },
            entityIn.pos,
            RayTraceContext.ShapeType.COLLIDER,
            RayTraceContext.FluidHandling.NONE,
            mc.player
        )
        return mc.world?.rayTrace(context)?.type == HitResult.Type.MISS
    }

    private enum class WaitMode {
        DYNAMIC, STATIC
    }
}
