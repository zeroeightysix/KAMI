package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.command.commands.BindCommand;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

/**
 * Created by 086 on 9/10/2018.
 */
public class Bind {

    boolean ctrl;
    boolean alt;
    boolean shift;
    int key;
    int scancode;

    public Bind(boolean ctrl, boolean alt, boolean shift, int key, int scancode) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.key = key;
        this.scancode = scancode;
    }

    public int getKey() {
        return key;
    }

    public int getScancode() {
        return scancode;
    }

    public boolean isCtrl() {
        return ctrl;
    }

    public boolean isAlt() {
        return alt;
    }

    public boolean isShift() {
        return shift;
    }

    public boolean isEmpty() {
        return !ctrl && !shift && !alt && key < 0;
    }

    public void setAlt(boolean alt) {
        this.alt = alt;
    }

    public void setCtrl(boolean ctrl) {
        this.ctrl = ctrl;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setShift(boolean shift) {
        this.shift = shift;
    }

    public String getName() {
        return isEmpty() ? "None" : GLFW.glfwGetKeyName(getKey(), getScancode());
    }

    @Override
    public String toString() {
        return isEmpty() ?
                "None" :
                (isCtrl() ? "Ctrl+" : "") +
                        (isAlt() ? "Alt+" : "") +
                        (isShift() ? "Shift+" : "") +
                        capitalise(getName());
    }

    public boolean isDown(int eventKey) {
        return !isEmpty() && (!BindCommand.modifiersEnabled.getValue() || (isShift() == isShiftDown()) && (isCtrl() == isCtrlDown()) && (isAlt() == isAltDown())) && eventKey == getKey();
    }

    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean isCtrlDown() {
        return Screen.hasControlDown();
    }

    public static boolean isAltDown() {
        return Screen.hasAltDown();
    }

    public String capitalise(String str) {
        if (str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + (str.length() != 1 ? str.substring(1).toLowerCase() : "");
    }

    public static Bind none() {
        return new Bind(false, false, false, -1, -1);
    }

}
