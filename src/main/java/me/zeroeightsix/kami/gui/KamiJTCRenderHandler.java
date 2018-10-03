package me.zeroeightsix.kami.gui;

import me.zeroeightsix.jtcui.handle.RenderHandler;
import org.lwjgl.opengl.GL11;

/**
 * Created by 086 on 3/10/2018.
 */
public class KamiJTCRenderHandler implements RenderHandler {

    double x = 0, y = 0;

    @Override
    public void scissor(double v, double v1, double v2, double v3) {

    }

    @Override
    public void disableScissor() {

    }

    @Override
    public void translate(double x, double y) {
        GL11.glTranslated(x, y, 0);
        this.x += x;
        this.y += y;
    }

    @Override
    public void resetTranslation() {
        GL11.glTranslated(-x, -y, 0);
        this.x = this.y = 0;
    }
}
