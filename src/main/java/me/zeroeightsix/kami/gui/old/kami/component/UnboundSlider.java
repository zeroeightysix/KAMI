package me.zeroeightsix.kami.gui.old.kami.component;

import me.zeroeightsix.kami.gui.old.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.old.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.old.rgui.component.use.Slider;

/**
 * Created by 086 on 16/12/2017.
 */
public class UnboundSlider extends AbstractComponent {

    double value;
    String text;
    public int sensitivity = 5;

    int originX;
    double originValue;

    boolean integer;
    double max = Double.MAX_VALUE;
    double min = Double.MIN_VALUE;

    public UnboundSlider(double value, String text, boolean integer) {
        this.value = value;
        this.text = text;
        this.integer = integer;

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                originX = event.getX();
                originValue = getValue();
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {
                originValue = getValue();
                originX = event.getX();
            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {
                int diff = (originX - event.getX())/sensitivity;
                setValue(Math.floor((originValue-(diff*(originValue==0 ? 1 : Math.abs(originValue)/10f)))*10f)/10f);
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) { }
            @Override
            public void onScroll(MouseScrollEvent event) {
                setValue(Math.round(getValue() + (event.isUp() ? 1 : -1)));
                originValue = getValue();
            }
        });
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setValue(double value) {
        if (min != Double.MIN_VALUE) value = Math.max(value,min);
        if (max != Double.MAX_VALUE) value = Math.min(value,max);
        Slider.SliderPoof.SliderPoofInfo info = new Slider.SliderPoof.SliderPoofInfo(this.value, value);
        callPoof(Slider.SliderPoof.class, info);
        this.value = integer ? Math.floor(info.getNewValue()) : info.getNewValue();
    }

    public double getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
