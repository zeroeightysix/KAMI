package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.gui.windows.Settings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Created by 086 on 9/10/2018.
 */
public class Bind {

    final boolean ctrl;
    final boolean alt;
    final boolean shift;

    public final Code code;

    public boolean pressed = false;

    public Bind(boolean ctrl, boolean alt, boolean shift, Code code) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.code = code;
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

    public static Bind none() {
        return new Bind(false, false, false, new Code(true, -1, -1));
    }

    public boolean isDown() {
        return pressed && (!Settings.INSTANCE.getModifiersEnabled() || (isShift() == isShiftDown()) && (isCtrl() == isCtrlDown()) && (isAlt() == isAltDown()));
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

    @Override
    public String toString() {
        return ((code.keysym && code.key == -1) || (!code.keysym && code.scan == -1)) ?
                "None" :
                (isCtrl() ? "Ctrl+" : "") +
                        (isAlt() ? "Alt+" : "") +
                        (isShift() ? "Shift+" : "") +
                        capitalise(code.toString());
    }

    public static class Code {
        public final boolean keysym;
        public final int key;
        public final int scan;

        public Code(boolean keysym, int key, int scan) {
            this.keysym = keysym;
            this.key = key;
            this.scan = scan;
        }

        public Code(@NotNull InputUtil.Key keyCode) {
            this.keysym = keyCode.getCategory() == InputUtil.Type.KEYSYM;
            int code = keyCode.getCode();
            this.key = keysym ? code : -1;
            this.scan = keysym ? -1 : code;
        }

        @Override
        public String toString() {
            if (keysym)
                return GLFW.glfwGetKeyName(key, -1);
            else
                return GLFW.glfwGetKeyName(-1, key);
        }
    }

}
