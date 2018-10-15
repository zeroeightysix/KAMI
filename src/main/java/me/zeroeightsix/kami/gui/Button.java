package me.zeroeightsix.kami.gui;

import me.zeroeightsix.jtcui.JTC;
import me.zeroeightsix.jtcui.Space;
import me.zeroeightsix.jtcui.component.SimpleComponent;
import me.zeroeightsix.jtcui.handle.EmptyComponentHandle;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by 086 on 15/10/2018.
 */
@JTC.Install(Button.ButtonHandle.class)
public class Button extends SimpleComponent {

    public Button(String text) {
        super(text);
        updateSizes();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        updateSizes();
    }

    private void updateSizes() {
        getRequirements().setMinimumWidth(Wrapper.getFontRenderer().getStringWidth(getText()));
    }

    static class ButtonHandle extends EmptyComponentHandle<Button> {

        @Override
        public void draw(Button component) {
            Space space = component.getSpace();
            int width = (int) component.getSpace().widthProperty().get(), height = (int) component.getSpace().heightProperty().get();
            GlStateManager.disableTexture2D();
            GlStateManager.color(.06f, .06f, .06f);
            RenderHelper.drawFilledRectangle(0, 0, width, height);
        }

    }

}
