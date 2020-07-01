package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.gui.windows.GraphicalSettings;
import me.zeroeightsix.kami.mixin.client.IKeyBinding;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Created by 086 on 9/10/2018.
 */
public class Bind {

    final boolean ctrl;
    final boolean alt;
    final boolean shift;
    public KeyBinding binding;

    public Bind(boolean ctrl, boolean alt, boolean shift, InputUtil.KeyCode code) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.binding = new KeyBinding("key.none", code.getCategory(), code.getKeyCode(), "key.categories.none");
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

    public KeyBinding getBinding() {
        return binding;
    }

    public String getKeyName() {
        return getBinding().getLocalizedName();
    }

    @Override
    public String toString() {
        return ((IKeyBinding) binding).getKeyCode().getKeyCode() == -1 ?
                "None" :
                (isCtrl() ? "Ctrl+" : "") +
                        (isAlt() ? "Alt+" : "") +
                        (isShift() ? "Shift+" : "") +
                        capitalise(getKeyName());
    }

    public boolean isDown() {
        return binding.isPressed() && (!GraphicalSettings.INSTANCE.getModifiersEnabled() || (isShift() == isShiftDown()) && (isCtrl() == isCtrlDown()) && (isAlt() == isAltDown()));
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
        return new Bind(false, false, false, InputUtil.getKeyCode(-1, -1));
    }

}
