package me.zeroeightsix.kami.gui.old.rgui.component.container;

import me.zeroeightsix.kami.gui.old.rgui.component.Component;
import me.zeroeightsix.kami.gui.old.rgui.layout.Layout;
import me.zeroeightsix.kami.gui.old.rgui.render.theme.Theme;

/**
 * Created by 086 on 26/06/2017.
 */
public class OrganisedContainer extends AbstractContainer {
    Layout layout;

    public OrganisedContainer(Theme theme, Layout layout) {
        super(theme);
        this.layout = layout;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public Container addChild(Component... component) {
        super.addChild(component);
        layout.organiseContainer(this);
        return this;
    }

    @Override
    public void setOriginOffsetX(int originoffsetX) {
        super.setOriginOffsetX(originoffsetX);
        layout.organiseContainer(this);
    }

    @Override
    public void setOriginOffsetY(int originoffsetY) {
        super.setOriginOffsetY(originoffsetY);
        layout.organiseContainer(this);
    }
}
