package me.zeroeightsix.kami.feature.module.render;

import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * Created by 086 on 25/01/2018.
 */
@Module.Info(name = "BossStack", description = "Modify the boss health GUI to take up less space", category = Module.Category.MISC)
public class BossStack extends Module {

    private Setting<BossStackMode> mode = register(Settings.e("Mode", BossStackMode.STACK));
    private Setting<Double> scale = Settings.d("Scale", .5d);

    //private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation("textures/gui/bars.png");

    /*public static void render(RenderGameOverlayEvent.Post event) {
        if (mode.getValue() == BossStackMode.MINIMIZE) {
            Map<UUID, BossInfoClient> map = MinecraftClient.getInstance().ingameGUI.getBossOverlay().mapBossInfos;
            if (map == null) return;
            ScaledResolution scaledresolution = new ScaledResolution(MinecraftClient.getInstance());
            int i = scaledresolution.getScaledWidth();
            int j = 12;

            for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                BossInfoClient info = entry.getValue();
                String text = info.getName().getFormattedText();

                int k = (int) ((i / scale.getValue()) / 2 - 91);
                GL11.glScaled(scale.getValue(), scale.getValue(), 1);
                if (!event.isCanceled()) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    MinecraftClient.getInstance().getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                    MinecraftClient.getInstance().ingameGUI.getBossOverlay().render(k, j, info);
                    MinecraftClient.getInstance().textRenderer.drawWithShadow(text, (float) ((i / scale.getValue()) / 2 - MinecraftClient.getInstance().textRenderer.getStringWidth(text) / 2), (float) (j - 9), 16777215);
                }
                GL11.glScaled(1d / scale.getValue(), 1d / scale.getValue(), 1);
                j += 10 + MinecraftClient.getInstance().textRenderer.FONT_HEIGHT;
            }
        } else if (mode.getValue() == BossStackMode.STACK) {
            Map<UUID, BossInfoClient> map = MinecraftClient.getInstance().ingameGUI.getBossOverlay().mapBossInfos;
            HashMap<String, Pair<BossInfoClient, Integer>> to = new HashMap<>();

            for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                String s = entry.getValue().getName().getFormattedText();
                if (to.containsKey(s)) {
                    Pair<BossInfoClient, Integer> p = to.get(s);
                    p = new Pair<>(p.getKey(), p.getValue() + 1);
                    to.put(s, p);
                } else {
                    Pair<BossInfoClient, Integer> p = new Pair<>(entry.getValue(), 1);
                    to.put(s, p);
                }
            }

            ScaledResolution scaledresolution = new ScaledResolution(MinecraftClient.getInstance());
            int i = scaledresolution.getScaledWidth();
            int j = 12;

            for (Map.Entry<String, Pair<BossInfoClient, Integer>> entry : to.entrySet()) {
                String text = entry.getKey();
                BossInfoClient info = entry.getValue().getKey();
                int a = entry.getValue().getValue();
                text += " x" + a;

                int k = (int) ((i / scale.getValue()) / 2 - 91);
                GL11.glScaled(scale.getValue(), scale.getValue(), 1);
                if (!event.isCanceled()) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    MinecraftClient.getInstance().getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                    MinecraftClient.getInstance().ingameGUI.getBossOverlay().render(k, j, info);
                    MinecraftClient.getInstance().textRenderer.drawWithShadow(text, (float) ((i / scale.getValue()) / 2 - MinecraftClient.getInstance().textRenderer.getStringWidth(text) / 2), (float) (j - 9), 16777215);
                }
                GL11.glScaled(1d / scale.getValue(), 1d / scale.getValue(), 1);
                j += 10 + MinecraftClient.getInstance().textRenderer.FONT_HEIGHT;
            }
        }
        return;
    }*/

    private enum BossStackMode {
        REMOVE, STACK, MINIMIZE
    }

}
