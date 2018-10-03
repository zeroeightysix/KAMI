package me.zeroeightsix.kami.gui.old.kami.component;

import me.zeroeightsix.kami.gui.old.kami.Stretcherlayout;
import me.zeroeightsix.kami.gui.old.rgui.component.container.AbstractContainer;
import me.zeroeightsix.kami.gui.old.rgui.component.container.use.Scrollpane;
import me.zeroeightsix.kami.gui.old.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.gui.old.rgui.component.use.InputField;
import me.zeroeightsix.kami.gui.old.rgui.component.use.Label;
import me.zeroeightsix.kami.gui.old.rgui.render.theme.Theme;
import org.lwjgl.input.Keyboard;

/**
 * Created by 086 on 2/08/2017.
 */
public class Chat extends AbstractContainer {

    Scrollpane scrollpane;
    Label label = new Label("", true);
    InputField field;

    public Chat(Theme theme, int width, int height) {
        super(theme);
        field = new InputField(width);
        scrollpane = new Scrollpane(getTheme(), new Stretcherlayout(1), width, height);
        scrollpane.setWidth(width);
        scrollpane.setHeight(height);
        scrollpane.setLockHeight(true).setLockWidth(true);
        scrollpane.addChild(label);

        field.addKeyListener(new KeyListener() {
            @Override
            public void onKeyDown(KeyEvent event) {
                if (event.getKey() == Keyboard.KEY_RETURN){
                    label.addLine(field.getText());
                    field.setText("");
                    if (scrollpane.canScrollY()){
                        scrollpane.setScrolledY(scrollpane.getMaxScrollY());
                    }
                }
            }

            @Override
            public void onKeyUp(KeyEvent event) {

            }
        });

        addChild(scrollpane);
        addChild(field);

        scrollpane.setLockHeight(false);
        scrollpane.setHeight(height - field.getHeight());
        scrollpane.setLockHeight(true);

        setWidth(width);
        setHeight(height);

        field.setY(getHeight() - field.getHeight());
    }

}
