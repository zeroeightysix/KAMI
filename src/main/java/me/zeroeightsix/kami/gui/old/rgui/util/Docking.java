package me.zeroeightsix.kami.gui.old.rgui.util;

/**
 * Created by 086 on 4/08/2017.
 */
public enum Docking {
    TOPLEFT(true, true, false, false),
    TOP(true,false,false,false),
    TOPRIGHT(true,false,false,true),
    RIGHT(false,false,false,true),
    BOTTOMRIGHT(false,false,true,true),
    BOTTOM(false,false,true,false),
    BOTTOMLEFT(false,true,true,false),
    LEFT(false,true,false,false),
    CENTER(false,false,false,false),
    NONE(false,false,false,false);

    boolean isTop, isLeft, isBottom, isRight;
    Docking(boolean isTop, boolean isLeft, boolean isBottom, boolean isRight) {
        this.isTop = isTop;
        this.isLeft = isLeft;
        this.isBottom = isBottom;
        this.isRight = isRight;
    }

    public boolean isBottom() {
        return isBottom;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isRight() {
        return isRight;
    }

    public boolean isTop() {
        return isTop;
    }
}
