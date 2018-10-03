package me.zeroeightsix.kami.gui.old.kami.component;

import me.zeroeightsix.kami.gui.old.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.gui.old.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * Created by 086 on 8/08/2017.
 */
public class BindButton extends EnumButton {

    static String[] lookingFor = new String[]{"_"};
    static String[] none = new String[]{"NONE"};
    boolean waiting = false;
    Module m;

    public BindButton(String name, Module m) {
        super(name, none);
        this.m = m;

        int key = m.getBind();
        if (key == -1)
            modes = none;
        else
            modes = new String[]{Keyboard.getKeyName(key)};

        addKeyListener(new KeyListener() {
            @Override
            public void onKeyDown(KeyEvent event) {
                if (!waiting) return;
                int key = event.getKey();

                if (key == Keyboard.KEY_BACK){
                    m.setKey(-1);
                    modes = none;
                    waiting = false;
                    return;
                }
                m.setKey(key);
                modes = new String[]{Keyboard.getKeyName(key)};
                waiting = false;
            }

            @Override
            public void onKeyUp(KeyEvent event) {

            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                setModes(lookingFor);
                waiting = true;
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {

            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {

            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {

            }
        });
    }
}
