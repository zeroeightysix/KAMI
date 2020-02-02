package me.zeroeightsix.kami.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;

/**
 * Created by 086 on 24/01/2018.
 */
@Module.Info(name = "ArmourHUD", category = Module.Category.RENDER)
public class ArmourHUD extends Module {

    private static ItemRenderer itemRender = MinecraftClient.getInstance().getItemRenderer();

    private Setting<Boolean> damage = register(Settings.b("Damage", false));

    @Override
    public void onRender() {
        GlStateManager.enableTexture();

        int i = MinecraftClient.getInstance().window.getScaledWidth() / 2;
        int iteration = 0;
        int y = MinecraftClient.getInstance().window.getScaledHeight() - 55 - (mc.player.isInWater() ? 10 : 0);
        for (ItemStack is : mc.player.inventory.armor) {
            iteration++;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepthTest();

            itemRender.zOffset = 200F;
            itemRender.renderGuiItemOverlay(mc.textRenderer, is, x, y);
            itemRender.renderGuiItem(is, x, y);
            itemRender.zOffset = 0F;

            GlStateManager.enableTexture();
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();

            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            mc.textRenderer.drawWithShadow(s, x + 19 - 2 - mc.textRenderer.getStringWidth(s), y + 9, 0xffffff);

            if (damage.getValue()) {
                float green = ((float) is.getMaxDamage() - (float) is.getDamage()) / (float) is.getMaxDamage();
                float red = 1 - green;
                int dmg = 100 - (int) (red * 100);
                mc.textRenderer.drawWithShadow(dmg + "", x + 8 - mc.textRenderer.getStringWidth(dmg + "") / 2, y - 11, ColourHolder.toHex((int) (red * 255), (int) (green * 255), 0));
            }
        }

        GlStateManager.enableDepthTest();
        GlStateManager.disableLighting();
    }
}
