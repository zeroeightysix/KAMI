package me.zeroeightsix.kami.util;

import org.lwjgl.opengl.GL11;

import java.awt.*;

public class HueCycler {

    int index = 0;
    int[] cycles;

    public HueCycler(int cycles) {
        if (cycles<=0) throw new IllegalArgumentException("cycles <= 0");
        this.cycles = new int[cycles];
        double hue = 0;
        double add = 1/(double)cycles;
        for (int i = 0; i < cycles; i++) {
            this.cycles[i] = Color.HSBtoRGB((float) hue, 1,1);
            hue += add;
        }
    }

    public void reset() {
        index = 0;
    }

    public void reset(int index) {
        this.index = index;
    }

    public int next() {
        int a = cycles[index];
        index++;
        if (index >= cycles.length) index = 0;
        return a;
    }

    public void setNext() {
        int rgb = next();
    }

    public void set() {
        int rgb = cycles[index];
        float red = ((rgb >> 16) & 0xFF)/255f;
        float green = ((rgb >> 8) & 0xFF)/255f;
        float blue = (rgb & 0xFF)/255f;
        GL11.glColor3f(red, green, blue);
    }

    public void setNext(float alpha) {
        int rgb = next();
        float red = ((rgb >> 16) & 0xFF)/255f;
        float green = ((rgb >> 8) & 0xFF)/255f;
        float blue = (rgb & 0xFF)/255f;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public int current() {
        return cycles[index];
    }
}
