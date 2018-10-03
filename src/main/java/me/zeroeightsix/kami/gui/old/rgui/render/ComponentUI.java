package me.zeroeightsix.kami.gui.old.rgui.render;

import me.zeroeightsix.kami.gui.old.rgui.component.Component;
import me.zeroeightsix.kami.gui.old.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.old.rgui.render.font.FontRenderer;

/**
 * Created by 086 on 25/06/2017.
 */
public interface ComponentUI<T extends Component> {

    public void renderComponent(T component, FontRenderer fontRenderer);
    public void handleMouseDown(T component, int x, int y, int button);
    public void handleMouseRelease(T component, int x, int y, int button);
    public void handleMouseDrag(T component, int x, int y, int button);
    public void handleScroll(T component, int x, int y, int amount, boolean up);

    public void handleKeyDown(T component, int key);
    public void handleKeyUp(T component, int key);

    public void handleAddComponent(T component, Container container);
    public void handleSizeComponent(T component);

    public Class<? extends Component> getHandledClass();

}
