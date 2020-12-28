package me.zeroeightsix.kami.feature.module

import imgui.ImGui
import imgui.StyleVar
import imgui.dsl
import imgui.internal.sections.ItemFlag
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import ladysnake.satin.api.managed.ManagedFramebuffer
import ladysnake.satin.api.managed.ManagedShaderEffect
import ladysnake.satin.api.managed.ShaderEffectManager
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.conditionalWrap
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.setting.ImGuiExtra
import me.zeroeightsix.kami.target.BlockCategory
import me.zeroeightsix.kami.target.BlockEntityCategory
import me.zeroeightsix.kami.target.BlockEntitySupplier
import me.zeroeightsix.kami.target.BlockSupplier
import me.zeroeightsix.kami.target.EntityCategory
import me.zeroeightsix.kami.target.EntitySupplier
import me.zeroeightsix.kami.wrapDisabled
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.util.Identifier
import kotlin.reflect.KProperty
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Listener as FiberListener

/**
 * @see me.zeroeightsix.kami.mixin.client.MixinWorldRenderer
 */
@Module.Info(
    name = "ESP",
    description = "Draws outlines around targeted entities, blocks, or block entities",
    category = Module.Category.RENDER
)
object ESP : Module() {

    @Setting
    var blurred = false

    val outlineShader: ManagedShaderEffect by object {
        val sharp: ManagedShaderEffect =
            ShaderEffectManager.getInstance().manage(Identifier("kami", "shaders/post/entity_sharp_outline.json"))
        val blur: ManagedShaderEffect =
            ShaderEffectManager.getInstance().manage(Identifier("kami", "shaders/post/entity_blur_outline.json"))

        operator fun getValue(thisRef: Any?, prop: KProperty<*>): ManagedShaderEffect =
            if (blurred) blur else sharp
    }
    val outlineFramebuffer: ManagedFramebuffer
        get() = outlineShader.getTarget("final")

    // Lazy so the init happens when everything is set up; otherwise entityVertexConsumers will be null
    val entityOutlineVertexConsumerProvider by lazy {
        OutlineVertexConsumerProvider(mc.bufferBuilders.entityVertexConsumers)
    }

    @Setting(name = "Entities")
    var entityTargets = EntitySupplier(
        mapOf(
            EntityCategory.ALL_PLAYERS to Colour.WHITE
        ),
        mapOf()
    )

    @Setting(name = "Block entities")
    var blockEntityTargets = BlockEntitySupplier(
        mapOf(
            BlockEntityCategory.CHESTS to Colour(1f, 0.92f, 0.81f, 0.28f) // Gold brownish
        ),
        mapOf()
    )

    @Transient
    var blocksChanged = false

    @Setting(name = "Blocks")
    @ImGuiExtra.Post("applyBlocksImGui")
    var blockTargets = BlockSupplier(
        mapOf(
            BlockCategory.ORES to ESPTarget()
        ),
        mapOf(
            BlockSupplier.SpecificBlock(Identifier("minecraft", "diamond_block")) to ESPTarget(outline = true)
        )
    )

    @FiberListener("Blocks")
    fun onBlocksChanged(new: BlockSupplier<ESPTarget>) {
        blocksChanged = true
    }

    @Suppress("unused")
    fun applyBlocksImGui() {
        wrapDisabled(!blocksChanged) {
            dsl.button("Reload") {
                blocksChanged = false
                mc.worldRenderer?.reload()
            }
        }
    }

    override fun onEnable() {
        mc.chunkCullingEnabled = false
    }

    override fun onDisable() {
        mc.chunkCullingEnabled = true
    }

    @GenerateType
    class ESPTarget(var solid: Boolean = true, var outline: Boolean = false, var outlineColour: Colour = Colour.WHITE)

}
