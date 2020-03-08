package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.TickEvent
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.LagCompensator
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RayTraceContext

/**
 * Created by 086 on 12/12/2017.
 * Updated by hub on 31 October 2019
 */
@Module.Info(
    name = "Aura",
    category = Module.Category.COMBAT,
    description = "Hits entities around you"
)
object Aura : Module() {
    private val attackPlayers =
        register(Settings.b("Players", true))
    private val attackMobs =
        register(Settings.b("Mobs", false))
    private val attackAnimals =
        register(Settings.b("Animals", false))
    private val hitRange =
        register(Settings.d("Hit Range", 5.5))
    private val ignoreWalls =
        register(Settings.b("Ignore Walls", true))
    private val waitMode =
        register(Settings.e<WaitMode>("Mode",
            WaitMode.DYNAMIC
        ))
    private val waitTick = register(
        Settings.integerBuilder("Tick Delay").withMinimum(0).withValue(3).withVisibility { o: Int? -> waitMode.value == WaitMode.STATIC }.build()
    )
    private val switchTo32k =
        register(Settings.b("32k Switch", true))
    private val onlyUse32k =
        register(Settings.b("32k Only", false))
    private var waitCounter = 0
    @EventHandler
    private val updateListener =
        Listener(EventHook<TickEvent.Client.InGame> {
            if (!mc.player.isAlive) {
                return@EventHook
            }
            val shield =
                mc.player.offHandStack.item == Items.SHIELD && mc.player.activeHand == Hand.OFF_HAND
            if (mc.player.isUsingItem && !shield) {
                return@EventHook
            }
            if (waitMode.value == WaitMode.DYNAMIC) {
                if (mc.player.getAttackCooldownProgress(lagComp) < 1) { // TODO: Is the right function?
                    return@EventHook
                } else if (mc.player.age % 2 != 0) {
                    return@EventHook
                }
            }
            if (waitMode.value == WaitMode.STATIC && waitTick.value > 0) {
                waitCounter = if (waitCounter < waitTick.value) {
                    waitCounter++
                    return@EventHook
                } else {
                    0
                }
            }
            for (target in MinecraftClient.getInstance().world.entities) {
                if (!EntityUtil.isLiving(target)) {
                    continue
                }
                if (target === mc.player) {
                    continue
                }
                if (mc.player.distanceTo(target) > hitRange.value) {
                    continue
                }
                if ((target as LivingEntity).health <= 0) {
                    continue
                }
                if (waitMode.value == WaitMode.DYNAMIC && target.hurtTime != 0) {
                    continue
                }
                if (!ignoreWalls.value && !mc.player.canSee(target) && !canEntityFeetBeSeen(
                        target
                    )
                ) {
                    continue  // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed
                }
                if (attackPlayers.value && target is PlayerEntity && !Friends.isFriend(target.getName().string)) {
                    attack(target)
                    return@EventHook
                } else {
                    if (if (EntityUtil.isPassive(target)) attackAnimals.value else EntityUtil.isMobAggressive(
                            target
                        ) && attackMobs.value
                    ) {
                        // We want to skip this if switchTo32k.getValue() is true,
                        // because it only accounts for tools and weapons.
                        // Maybe someone could refactor this later? :3
                        if (!switchTo32k.value && AutoTool.isEnabled()) {
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
            val enchantment = enchantments.getCompoundTag(i)
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
        if (checkSharpness(mc.player.activeItem)) {
            holding32k = true
        }
        if (switchTo32k.value && !holding32k) {
            var newSlot = -1
            for (i in 0..8) {
                val stack = mc.player.inventory.getInvStack(i)
                if (stack == ItemStack.EMPTY) {
                    continue
                }
                if (checkSharpness(stack)) {
                    newSlot = i
                    break
                }
            }
            if (newSlot != -1) {
                mc.player.inventory.selectedSlot = newSlot
                holding32k = true
            }
        }
        if (onlyUse32k.value && !holding32k) {
            return
        }
        mc.interactionManager.attackEntity(
            mc.player,
            e
        )
        mc.player.swingHand(Hand.MAIN_HAND)
    }

    private val lagComp: Float
        private get() = if (waitMode.value == WaitMode.DYNAMIC) {
            -(20 - LagCompensator.INSTANCE.tickRate)
        } else 0.0f

    private fun canEntityFeetBeSeen(entityIn: Entity): Boolean {
        val context = RayTraceContext(
            mc.player.pos.add(
                0.0,
                mc.player.getEyeHeight(mc.player.pose).toDouble(),
                0.0
            ),
            entityIn.pos,
            RayTraceContext.ShapeType.COLLIDER,
            RayTraceContext.FluidHandling.NONE,
            mc.player
        )
        return mc.world.rayTrace(context).type == HitResult.Type.MISS
    }

    private enum class WaitMode {
        DYNAMIC, STATIC
    }
}