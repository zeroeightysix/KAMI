package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.gui.kami.Stretcherlayout;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.OrganisedContainer;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.AbstractSetting;
import me.zeroeightsix.kami.setting.Named;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.impl.BooleanSetting;
import me.zeroeightsix.kami.setting.impl.EnumSetting;
import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting;
import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting;
import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;
import me.zeroeightsix.kami.util.Bind;

import java.util.Arrays;

/**
 * Created by 086 on 6/08/2017.
 */
public class SettingsPanel extends OrganisedContainer {

    Module module;

    public SettingsPanel(Theme theme, Module module) {
        super(theme, new Stretcherlayout(1));
        setAffectLayout(false);
        this.module = module;
        prepare();
    }

    @Override
    public void renderChildren() {
        super.renderChildren();
    }

    public Module getModule() {
        return module;
    }

    private void prepare() {
        getChildren().clear();
        if (module == null) {
            setVisible(false);
            return;
        }
        if (!module.settingList.isEmpty()) {
            for (Setting setting : module.settingList) {
                if (!(setting instanceof Named) || ((setting instanceof AbstractSetting) && !setting.isVisible())) continue;
                String name = ((Named) setting).getName();
                boolean isNumber = setting instanceof NumberSetting;
                boolean isBoolean = setting instanceof BooleanSetting;
                boolean isEnum = setting instanceof EnumSetting;

                if (setting.getValue() instanceof Bind) {
                    addChild(new BindButton("Bind", module));
                }

                if (isNumber) {
                    NumberSetting numberSetting = (NumberSetting) setting;
                    boolean isBound = numberSetting.isBound();

                    if (!isBound) {
                        UnboundSlider slider = new UnboundSlider(numberSetting.getValue().doubleValue(), name, setting instanceof IntegerSetting);
                        slider.addPoof(new Slider.SliderPoof<UnboundSlider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(UnboundSlider component, SliderPoofInfo info) {
                                if (setting instanceof IntegerSetting)
                                    setting.setValue(new Integer((int) info.getNewValue()));
                                else if (setting instanceof FloatSetting)
                                    setting.setValue(new Float(info.getNewValue()));
                                else if (setting instanceof DoubleSetting)
                                    setting.setValue(info.getNewValue());
                                setModule(module);
                            }
                        });
                        if (numberSetting.getMax() != null) slider.setMax(numberSetting.getMax().doubleValue());
                        if (numberSetting.getMin() != null) slider.setMin(numberSetting.getMin().doubleValue());
                        addChild(slider);
                    } else {
                        Slider slider = new Slider(numberSetting.getValue().doubleValue(), numberSetting.getMin().doubleValue(), numberSetting.getMax().doubleValue(), Slider.getDefaultStep(numberSetting.getMin().doubleValue(), numberSetting.getMax().doubleValue()), name, setting instanceof IntegerSetting);
                        slider.addPoof(new Slider.SliderPoof<Slider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(Slider component, SliderPoofInfo info) {
                                if (setting instanceof IntegerSetting)
                                    setting.setValue(new Integer((int) info.getNewValue()));
                                else if (setting instanceof FloatSetting)
                                    setting.setValue(new Float(info.getNewValue()));
                                else if (setting instanceof DoubleSetting)
                                    setting.setValue(info.getNewValue());
                                setModule(module);
                            }
                        });
                        addChild(slider);
                    }
                }else if(isBoolean) {
                    CheckButton checkButton = new CheckButton(name);
                    checkButton.setToggled(((BooleanSetting) setting).getValue());
                    checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                        @Override
                        public void execute(CheckButton checkButton1, CheckButtonPoofInfo info) {
                            if (info.getAction() == CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE) {
                                setting.setValue(checkButton.isToggled());
                                setModule(module);
                            }
                        }
                    });
                    addChild(checkButton);
                }else if(isEnum) {
                    Class<? extends Enum> type = ((EnumSetting) setting).clazz;
                    Object[] con = type.getEnumConstants();
                    String[] modes = Arrays.stream(con).map(o -> o.toString().toUpperCase()).toArray(String[]::new);
                    EnumButton enumbutton = new EnumButton(name, modes);
                    enumbutton.addPoof(new EnumButton.EnumbuttonIndexPoof<EnumButton, EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo>() {
                        @Override
                        public void execute(EnumButton component, EnumbuttonInfo info) {
                            setting.setValue(con[info.getNewIndex()]);
                            setModule(module);
                        }
                    });
                    enumbutton.setIndex(Arrays.asList(con).indexOf(setting.getValue()));
                    addChild(enumbutton);
                }
            }
        }
        if (children.isEmpty()) {
            setVisible(false);
            return;
        }else{
            setVisible(true);
            return;
        }
    }

    public void setModule(Module module) {
        this.module = module;
        setMinimumWidth((int) (getParent().getWidth()*.9f));
        prepare();

        setAffectLayout(false);
        for (Component component : children){
            component.setWidth(getWidth()-10);
            component.setX(5);
        }
    }
}
