package me.zeroeightsix.kami.gui.old.rgui.component.container;

import me.zeroeightsix.kami.gui.old.rgui.component.Component;

import java.util.ArrayList;

/**
 * Created by 086 on 25/06/2017.
 */
public interface Container extends Component {
    public ArrayList<Component> getChildren();
    public Component getComponentAt(int x, int y);
    public Container addChild(Component... component);
    public Container removeChild(Component component);
    public boolean hasChild(Component component);
    public void renderChildren();

    public int getOriginOffsetX();
    public int getOriginOffsetY();

    public boolean penetrateTest(int x, int y);
}
