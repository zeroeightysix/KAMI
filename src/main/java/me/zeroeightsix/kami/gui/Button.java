package me.zeroeightsix.kami.gui;

import me.zeroeightsix.jtcui.JTC;
import me.zeroeightsix.jtcui.Space;
import me.zeroeightsix.jtcui.component.SimpleComponent;
import me.zeroeightsix.jtcui.handle.EmptyComponentHandle;
import me.zeroeightsix.jtcui.handle.InputHandler;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by 086 on 15/10/2018.
 */
@JTC.Install(Button.ButtonHandle.class)
public class Button extends SimpleComponent {

    boolean hovered = false;

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
        getRequirements().setMinimumWidth(Wrapper.getFontRenderer().getStringWidth(getText()) + 12);
        getRequirements().setMinimumHeight(Wrapper.getFontRenderer().FONT_HEIGHT+6);

        if (getSpace().widthProperty().get() < getRequirements().getMinimumWidth()) getSpace().widthProperty().set(getRequirements().getMinimumWidth());
        if (getSpace().heightProperty().get() < getRequirements().getMinimumHeight()) getSpace().heightProperty().set(getRequirements().getMinimumHeight());
    }

    public static class ButtonHandle extends EmptyComponentHandle<Button> {

        @Override
        public void onMouse(Button component, InputHandler.MouseAction action, int x, int y, int button) {
            super.onMouse(component, action, x, y, button);
            switch (action) {
                case DOWN:
                    component.getAction().accept(component);
                    break;
                case ENTER_COMPONENT:
                    component.hovered = true;
                    break;
                case LEAVE_COMPONENT:
                    component.hovered = false;
                    break;
            }
        }

        @Override
        public void draw(Button component) {
            Space space = component.getSpace();
            double width = component.getSpace().widthProperty().get();
            double height = component.getSpace().heightProperty().get();

            GlStateManager.disableTexture2D();
            GlStateManager.color(0.17f, 0.20f, 0.23f);
            RenderHelper.drawFilledRectangle(0, 0, width, height);
            GlStateManager.color(0.13f, 0.18f, 0.24f);
            GlStateManager.enableTexture2D();
            Wrapper.getFontRenderer().drawString(component.getText(), (int) (width / 2 - (Wrapper.getFontRenderer().getStringWidth(component.getText()) / 2)), 2, component.hovered ? 0x68717D : 0x4B5460);

        }

    }

}
