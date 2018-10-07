package me.zeroeightsix.kami.gui.old.kami;

import me.zeroeightsix.jtcui.handle.InputHandler;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import static me.zeroeightsix.kami.KamiMod.jtc;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Created by 086 on 3/08/2017.
 */
public class DisplayGuiScreen extends GuiScreen {

    KamiGUI gui;
    public final GuiScreen lastScreen;

    public static int mouseX;
    public static int mouseY;

    private boolean mouseState;
    private int mouseButton;

    public static double scale = 2;

    public DisplayGuiScreen(GuiScreen lastScreen) {
        this.lastScreen = lastScreen;

//        KamiGUI gui = KamiMod.getInstance().getGuiManager();
//
//        for (Component c : gui.getChildren()){
//            if (c instanceof Frame){
//                Frame child = (Frame) c;
//                if (child.isPinneable() && child.isVisible()){
//                    child.setOpacity(.5f);
//                }
//            }
//        }
    }

    @Override
    public void onGuiClosed() {
//        KamiGUI gui = KamiMod.getInstance().getGuiManager();
//
//        gui.getChildren().stream().filter(component -> (component instanceof Frame) && (((Frame) component).isPinneable()) && component.isVisible()).forEach(component -> component.setOpacity(0f));
    }

    @Override
    public void initGui() {
        gui = KamiMod.getInstance().getGuiManager();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        calculateMouse();
        GlStateManager.enableTexture2D();
        double scale = getScale() / DisplayGuiScreen.scale;
        GlStateManager.scale(1d / scale, 1d / scale, 1);
        jtc.renderRecursive();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.mouseButton = mouseButton;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    @Override
    public void updateScreen() {
        if (Mouse.hasWheel()){
            int a = Mouse.getDWheel();
            if (a != 0){
                jtc.input.onScroll(a, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE)
            mc.displayGuiScreen(lastScreen);
        else{
            jtc.input.onKey(InputHandler.KeyAction.DOWN, keyCode, typedChar);
            jtc.input.onKey(InputHandler.KeyAction.RELEASE, keyCode, typedChar);
        }
    }

    public static int getScale(){
        int scale = Wrapper.getMinecraft().gameSettings.guiScale;
        if(scale == 0)
            scale = 1000;
        int scaleFactor = 0;
        while(scaleFactor < scale && Wrapper.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Wrapper.getMinecraft().displayHeight / (scaleFactor + 1) >= 240)
            scaleFactor++;
        if (scaleFactor == 0)
            scaleFactor = 1;
        return scaleFactor;
    }

    private void calculateMouse() {
        Minecraft minecraft = Minecraft.getMinecraft();
        int prevX = mouseX;
        int prevY = mouseY;
        boolean prevS = mouseState;

        mouseX = Mouse.getX();
        mouseY = minecraft.displayHeight - Mouse.getY() - 1;
        mouseX /= scale;
        mouseY /= scale;
        mouseState = Mouse.isButtonDown(mouseButton);

        if (prevS != mouseState) {
            if (mouseState)
                jtc.input.onMouse(InputHandler.MouseAction.DOWN, mouseX, mouseY, mouseButton);
            else
                jtc.input.onMouse(InputHandler.MouseAction.RELEASE, mouseX, mouseY, mouseButton);
            return;
        }

        if (prevX != mouseX || prevY != mouseY) {
            if (mouseState)
                jtc.input.onMouse(InputHandler.MouseAction.DRAG, mouseX, mouseY, mouseButton);
            else
                jtc.input.onMouse(InputHandler.MouseAction.MOVE, mouseX, mouseY, mouseButton);
        }
    }

}
