package me.zeroeightsix.kami.gui.old.kami.theme.kami;

import me.zeroeightsix.kami.gui.old.kami.KamiGUI;
import me.zeroeightsix.kami.gui.old.kami.theme.staticui.RadarUI;
import me.zeroeightsix.kami.gui.old.kami.theme.staticui.TabGuiUI;
import me.zeroeightsix.kami.gui.old.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.old.rgui.component.use.Button;
import me.zeroeightsix.kami.gui.old.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.old.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.gui.old.rgui.render.theme.AbstractTheme;

/**
 * Created by 086 on 26/06/2017.
 */
public class KamiTheme extends AbstractTheme {

    FontRenderer fontRenderer;

    public KamiTheme() {
        installUI(new RootButtonUI<Button>());
        installUI(new GUIUI());
        installUI(new RootGroupboxUI());
        installUI(new KamiFrameUI<Frame>());
        installUI(new RootScrollpaneUI());
        installUI(new RootInputFieldUI());
        installUI(new RootLabelUI());
        installUI(new RootChatUI());
        installUI(new RootCheckButtonUI());
        installUI(new KamiActiveModulesUI());
        installUI(new KamiSettingsPanelUI());
        installUI(new RootSliderUI());
        installUI(new KamiEnumbuttonUI());
        installUI(new RootColorizedCheckButtonUI());
        installUI(new KamiUnboundSliderUI());

        installUI(new RadarUI());
        installUI(new TabGuiUI());

        fontRenderer=KamiGUI.fontRenderer;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public class GUIUI extends AbstractComponentUI<KamiGUI> {
    }
}
