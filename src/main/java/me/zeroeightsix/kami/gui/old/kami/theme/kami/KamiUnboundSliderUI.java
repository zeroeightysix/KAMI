package me.zeroeightsix.kami.gui.old.kami.theme.kami;

import me.zeroeightsix.kami.gui.old.kami.component.UnboundSlider;
import me.zeroeightsix.kami.gui.old.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.old.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.old.rgui.render.font.FontRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Created by 086 on 17/12/2017.
 */
public class KamiUnboundSliderUI extends AbstractComponentUI<UnboundSlider> {

    @Override
    public void renderComponent(UnboundSlider component, FontRenderer fontRenderer) {
        String s = component.getText() + ": " + component.getValue();
        int c = component.isPressed() ? 0xaaaaaa : 0xdddddd;
        if (component.isHovered())
            c = (c & 0x7f7f7f) << 1;
        fontRenderer.drawString(component.getWidth()/2-fontRenderer.getStringWidth(s)/2, component.getHeight() - fontRenderer.getFontHeight()/2-4, c, s);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void handleAddComponent(UnboundSlider component, Container container) {
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight());
        component.setWidth(component.getTheme().getFontRenderer().getStringWidth(component.getText()));
    }

}
