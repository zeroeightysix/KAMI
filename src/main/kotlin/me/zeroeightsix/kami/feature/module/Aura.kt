package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.target.EntityCategory
import me.zeroeightsix.kami.target.EntitySupplier
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext

@Module.Info(
    name = "Aura",
    category = Module.Category.COMBAT,
    description = "Hits entities around you"
)
object Aura : Module() {

    @Setting
    var targets = EntitySupplier(
        mapOf(
            EntityCategory.NONFRIENDLY_PLAYERS to Unit
        ),
        mapOf()
    )

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

    @Setting(name = "Only use 32k")
    var onlyUse32k: Boolean = false

    @Setting(name = "Ignore nametagged")
    var filterNametags = true

    private var waitCounter = 0

    @Suppress("UNUSED")
    fun ifModeStatic() = waitMode == WaitMode.STATIC

    @EventHandler
    private val updateListener =
        Listener<TickEvent.InGame>({
            val player = it.player
            if (!player.isAlive) {
                return@Listener
            }
            val shield =
                player.offHandStack.item == Items.SHIELD && player.activeHand == Hand.OFF_HAND
            if (player.isUsingItem && !shield) {
                return@Listener
            }
            if (waitMode == WaitMode.DYNAMIC) {
                if (player.getAttackCooldownProgress(0f) < 1) {
                    return@Listener
                }
            }
            if (waitMode == WaitMode.STATIC && waitTick > 0) {
                waitCounter = if (waitCounter < waitTick) {
                    waitCounter++
                    return@Listener
                } else {
                    0
                }
            }
            targets.targets.forEach { (entity, _) ->
                val living = entity as? LivingEntity ?: return@forEach
                if (living.health <= 0 ||
                    (waitMode == WaitMode.DYNAMIC && entity.hurtTime != 0) ||
                    (player.distanceTo(entity) > hitRange) ||
                    (!ignoreWalls && !player.canSee(entity) && !canEntityFeetBeSeen(player, entity)) ||
                    (filterNametags && entity.hasCustomName())
                ) {
                    return@forEach
                }

                // We want to skip this if switchTo32k is true,
                // because it only accounts for tools and weapons.
                // Maybe someone could refactor this later? :3
                if (!switchTo32k && AutoTool.enabled) {
                    AutoTool.equipBestWeapon()
                }

                attack(player, living, onlyUse32k)
                return@Listener // We've attacked, so let's wait until the next tick to attack again.
            }
        })

    private fun checkSharpness(stack: ItemStack): Boolean {
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

    private fun attack(player: ClientPlayerEntity, e: Entity, onlyUse32k: Boolean) {
        var holding32k = false
        if (player.activeItem?.let { checkSharpness(it) }!!) {
            holding32k = true
        }
        if (switchTo32k && !holding32k) {
            var newSlot = -1
            for (i in 0..8) {
                val stack = player.inventory?.getStack(i)
                if (stack == ItemStack.EMPTY) {
                    continue
                }
                if (stack?.let { checkSharpness(it) }!!) {
                    newSlot = i
                    break
                }
            }
            if (newSlot != -1) {
                player.inventory?.selectedSlot = newSlot
                holding32k = true
            }
        }
        if (onlyUse32k && !holding32k) {
            return
        }
        mc.interactionManager?.attackEntity(
            player,
            e
        )
        player.swingHand(Hand.MAIN_HAND)
    }

    private fun canEntityFeetBeSeen(by: ClientPlayerEntity, entityIn: Entity): Boolean {
        val context = RaycastContext(
            by.getEyeHeight(by.pose).toDouble().let {
                by.pos?.add(
                    0.0,
                    it,
                    0.0
                )
            },
            entityIn.pos,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            by
        )
        return mc.world?.raycast(context)?.type == HitResult.Type.MISS
    }

    private enum class WaitMode {
        DYNAMIC, STATIC
    }
}
