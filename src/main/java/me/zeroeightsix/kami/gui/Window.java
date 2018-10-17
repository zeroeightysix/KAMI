package me.zeroeightsix.kami.gui;

import me.zeroeightsix.jtcui.Fat;
import me.zeroeightsix.jtcui.JTC;
import me.zeroeightsix.jtcui.component.AbstractDraggable;
import me.zeroeightsix.jtcui.handle.DraggableHandle;
import me.zeroeightsix.jtcui.handle.InputHandler;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * Created by 086 on 3/10/2018.
 */
@JTC.Install(Window.WindowHandle.class)
public class Window extends AbstractDraggable {

    private final int titleHeight;

    private boolean resizing = false;

    public Window(String title, int titleHeight, int borderThickness) {
        this(0, 0, 0, 0, title, titleHeight, borderThickness);
    }

    public Window(int x, int y, int width, int height, String title, int titleHeight, int borderThickness) {
        super(x, y, width, height, new Fat(borderThickness, borderThickness, borderThickness + titleHeight, borderThickness), 0, 0, width, titleHeight);
        this.titleHeight = titleHeight;
        setText(title);
    }

    public static class WindowHandle extends DraggableHandle<Window> {

        @Override
        public void draw(Window component) {
            double width = component.getSpace().widthProperty().get();
            double height = component.getSpace().heightProperty().get();

            /*GlStateManager.disableTexture2D();
            GlStateManager.color(.17f,.17f,.18f,.9f);
            RenderHelper.drawFilledRectangle(0,0,width,height);
            GlStateManager.color(.59f,.05f,.11f);
            GlStateManager.glLineWidth(1.5f);
            RenderHelper.drawRectangle(0,0,width,height);
            GlStateManager.color(1,1,1);*/

            GlStateManager.disableTexture2D();
            GlStateManager.color(0.19f, 0.22f, 0.25f);
            RenderHelper.drawFilledRectangle(0, 0, width, height);

            if (component.resizing) {
                GlStateManager.color(1, 0.64f, 0);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex2d(width, height - 4);
                GL11.glVertex2d(width, height);
                GL11.glVertex2d(width, height);
                GL11.glVertex2d(width - 4, height);
                GL11.glEnd();
            }

            GlStateManager.disableBlend();

            String displayText = component.getText();
            int stringWidth;
            while ((stringWidth = Wrapper.getFontRenderer().getStringWidth(displayText)) > component.getSpace().widthProperty().get() && displayText.length()>=3) {
                displayText = displayText.substring(0, displayText.length() - 3) + ".";
            }
            GlStateManager.enableTexture2D();

            Wrapper.getMinecraft().fontRenderer.drawString(displayText, (int) (component.getSpace().widthProperty().get() / 2 - stringWidth / 2), component.titleHeight / 2 - Wrapper.getMinecraft().fontRenderer.FONT_HEIGHT / 2, 0x7F8388);
        }

        @Override
        public void onMouse(Window component, InputHandler.MouseAction action, int x, int y, int button) {
            super.onMouse(component, action, x, y, button);
            if (action == InputHandler.MouseAction.MOVE) {
                boolean bottom = y > component.getSpace().heightProperty().get() - 5;
                component.resizing = x > component.getSpace().widthProperty().get() - 5 && bottom;
            } else if (action == InputHandler.MouseAction.DRAG && component.resizing) {
                component.getSpace().heightProperty().set(y);
                component.getSpace().widthProperty().set(x);
            } else if (action == InputHandler.MouseAction.LEAVE_COMPONENT) {
                component.resizing = false;
            }
        }

    }

}
