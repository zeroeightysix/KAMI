package me.zeroeightsix.kami.gui.rgui.util;

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
    CENTER(true, true, true, true),
    NONE(false,false,false,false),
    CENTERTOP(true, true, false, true),
    CENTERBOTTOM(false, false, true, false),
    CENTERVERTICAL(false, true, false, true),
    CENTERHOIZONTAL(true, false, true, false),
    CENTERLEFT(true, true, true, false),
    CENTERRIGHT(true, false, true, true);

    boolean isTop, isLeft, isBottom, isRight;
    Docking(boolean isTop, boolean isLeft, boolean isBottom, boolean isRight) {
        this.isTop = isTop;
        this.isLeft = isLeft;
        this.isBottom = isBottom;
        this.isRight = isRight;
    }

    public boolean isBottom() {
        return isBottom && !isTop;
    }

    public boolean isLeft() {
        return isLeft && !isRight;
    }

    public boolean isRight() {
        return isRight && !isLeft;
    }

    public boolean isTop() {
        return isTop && !isBottom;
    }

    public boolean isCenterHorizontal() {
        return isLeft && isRight;
    }

    public boolean isCenterVertical() {
        return isTop && isBottom;
    }
}
