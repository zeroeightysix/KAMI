package me.zeroeightsix.kami.gui;

import me.zeroeightsix.jtcui.Fat;
import me.zeroeightsix.jtcui.JTC;
import me.zeroeightsix.jtcui.component.AbstractDraggable;
import me.zeroeightsix.jtcui.handle.DraggableHandle;
import me.zeroeightsix.jtcui.handle.InputHandler;
import me.zeroeightsix.kami.gui.old.kami.RenderHelper;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * Created by 086 on 3/10/2018.
 */
@JTC.Install(Window.WindowHandle.class)
public class Window extends AbstractDraggable {

    private final int titleHeight;

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
            GlStateManager.disableTexture2D();
            GL11.glColor3f(.1f, .1f, .1f);
            RenderHelper.drawFilledRectangle(0, 0, component.getSpace().widthProperty().get(), component.getSpace().heightProperty().get());
            GL11.glColor3f(.9f, .9f, .9f);
            RenderHelper.drawFilledRectangle(component.getFat().getLeft(), component.getFat().getTop(), component.getSpace().widthProperty().get() - component.getFat().getLeft() - component.getFat().getRight(), component.getSpace().heightProperty().get() - component.getFat().getBottom() - component.getFat().getTop());
            GlStateManager.enableTexture2D();
            Wrapper.getFontRenderer().drawString(5, component.titleHeight / 2 - 5, component.getText());
        }

        @Override
        public void onMouse(Window component, InputHandler.MouseAction action, int x, int y, int button) {
            super.onMouse(component, action, x, y, button);
        }

    }

}
