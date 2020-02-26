package me.zeroeightsix.kami.module

import com.mojang.blaze3d.platform.GlStateManager
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.event.events.RenderHudEvent
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud.renderHud
import me.zeroeightsix.kami.mixin.client.IKeyBinding
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.ClassFinder
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.opengl.GL11
import java.lang.reflect.InvocationTargetException

/**
 * Created by 086 on 23/08/2017.
 */
object FeatureManager {

    val features = mutableListOf<Feature>()
    // Because we also handle module-only events (deprecated), we pre-sort them.
    @Deprecated("Use events instead.")
    val modules = mutableListOf<Module>()
    /**
     * Lookup map for getting by name
     */
    var lookup = mutableMapOf<String, Feature>()
    
    @EventHandler
    var worldRenderListener =
        Listener(
            EventHook { event: RenderEvent.World ->
                onWorldRender(event)
                KamiTessellator.releaseGL()
            }
        )

    @EventHandler
    var renderHudEventListener =
        Listener(
            EventHook<RenderHudEvent> {
                onRender()
                if (MinecraftClient.getInstance().currentScreen !is KamiGuiScreen) {
                    renderHud()
                }
            }
        )

    fun initialize() {
        initPlay()
    }

    @JvmStatic
    fun updateLookup() {
        lookup.clear()
        for (m in features) lookup[m.name.value.toLowerCase()] = m
    }

    @Deprecated(message = "Use event listeners instead.")
    fun onRender() {
        modules.stream()
            .filter { it.alwaysListening || it.isEnabled() }
            .forEach { it.onRender() }
    }

    @Deprecated(message = "Use event listeners instead.")
    fun onWorldRender(event: RenderEvent.World) {
        MinecraftClient.getInstance().profiler.push("kami")
        MinecraftClient.getInstance().profiler.push("setup")
        GlStateManager.disableTexture()
        GlStateManager.enableBlend()
        GlStateManager.disableAlphaTest()
        GlStateManager.blendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            1,
            0
        )
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        GlStateManager.disableDepthTest()
        GlStateManager.lineWidth(1f)
        val renderPos =
            EntityUtil.getInterpolatedPos(Wrapper.getPlayer(), event.partialTicks)
        MinecraftClient.getInstance().profiler.pop()
        modules.stream()
            .filter { it.alwaysListening || it.isEnabled() }
            .forEach {
                MinecraftClient.getInstance().profiler.push(it.name.value)
                it.onWorldRender(event)
                MinecraftClient.getInstance().profiler.pop()
            }
        MinecraftClient.getInstance().profiler.push("release")
        GlStateManager.lineWidth(1f)
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.disableBlend()
        GlStateManager.enableAlphaTest()
        GlStateManager.enableTexture()
        GlStateManager.enableDepthTest()
        GlStateManager.enableCull()
        KamiTessellator.releaseGL()
        MinecraftClient.getInstance().profiler.pop()
        MinecraftClient.getInstance().profiler.pop()
    }

    @JvmStatic
    fun onBind(key: Int, scancode: Int, i: Int) {
        val pressed = i != 0
        val code = InputUtil.getKeyCode(key, scancode)
        if (Wrapper.getMinecraft().currentScreen != null) {
            return
        }
        if (key == 89 && scancode == 29) {
            if (KamiMod.getInstance().kamiGuiScreen == null) {
                KamiMod.getInstance().kamiGuiScreen = KamiGuiScreen()
            }
            Wrapper.getMinecraft().openScreen(KamiMod.getInstance().kamiGuiScreen)
        }
        modules.stream().forEach {
            val bind = it.bind
            if ((bind.binding as IKeyBinding).keyCode == code) {
                (bind.binding as IKeyBinding).setPressed(pressed)
            }
            if (it.bind.isDown) {
                it.toggle()
            }
        }
    }

    @JvmStatic
    fun getModuleByName(name: String): Module? {
        return lookup[name.toLowerCase()] as Module
    }

    fun addFeature(feature: Feature): Boolean {
        return features.add(feature)
    }

    fun removeFeature(feature: Feature): Boolean {
        return features.remove(feature)
    }

    @JvmStatic
    fun isModuleEnabled(moduleName: String): Boolean {
        val m = getModuleByName(moduleName) ?: return false
        return m.isEnabled()
    }

    private fun initPlay() {
        ClassFinder.findClasses(
            ClickGUI::class.java.getPackage().name, Module::class.java
        ).forEach {
            try {
                val module = it.getConstructor().newInstance() as Module
                features.add(module)
                modules.add(module)
            } catch (e: InvocationTargetException) {
                e.cause!!.printStackTrace()
                System.err.println("Couldn't initiate module " + it.simpleName + "! Err: " + e.javaClass.simpleName + ", message: " + e.message)
            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println("Couldn't initiate module " + it.simpleName + "! Err: " + e.javaClass.simpleName + ", message: " + e.message)
            }
        }

        KamiMod.log.info("Modules initialised")
        features.sortWith(compareBy { it.name.value })
    }

}