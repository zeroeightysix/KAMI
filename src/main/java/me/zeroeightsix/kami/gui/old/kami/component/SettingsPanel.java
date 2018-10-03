package me.zeroeightsix.kami.gui.old.kami.component;

import me.zeroeightsix.kami.gui.old.kami.Stretcherlayout;
import me.zeroeightsix.kami.gui.old.rgui.component.Component;
import me.zeroeightsix.kami.gui.old.rgui.component.container.OrganisedContainer;
import me.zeroeightsix.kami.gui.old.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.old.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.old.rgui.render.theme.Theme;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.SettingsClass;

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
        if (!module.getSettings().isEmpty()) {
            for (SettingsClass.StaticSetting staticSetting : module.getSettings()) {
                Class type = staticSetting.getField().getType();
                boolean isNumber = type == int.class || type == double.class || type == float.class || type == short.class || type == long.class || type == byte.class;
                boolean isBoolean = type == boolean.class;
                boolean isEnum = type.isEnum();
                Setting settingAnnotation = staticSetting.getField().getAnnotation(Setting.class);
                if (settingAnnotation.hidden()) {
                    if (settingAnnotation.name().equalsIgnoreCase("Bind")) {
                        addChild(new BindButton("Bind", module));
                    }
                    continue;
                }

                if (isNumber) {
                    boolean isBound = settingAnnotation.max() != -1 && settingAnnotation.min() != -1;

                    if (!isBound) {
                        UnboundSlider slider = new UnboundSlider(Double.parseDouble(staticSetting.getValue().toString()), staticSetting.getDisplayName(), settingAnnotation.integer());
                        slider.addPoof(new Slider.SliderPoof<UnboundSlider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(UnboundSlider component, SliderPoofInfo info) {
                                staticSetting.setValue(info.getNewValue());
                            }
                        });
                        if (settingAnnotation.max() != -1) slider.setMax(settingAnnotation.max());
                        if (settingAnnotation.min() != -1) slider.setMin(settingAnnotation.min());
                        addChild(slider);
                    } else {
                        Slider slider = new Slider(Double.parseDouble(staticSetting.getValue().toString()), settingAnnotation.min(), settingAnnotation.max(), Slider.getDefaultStep(settingAnnotation.min(), settingAnnotation.max()), staticSetting.getDisplayName(), settingAnnotation.integer());
                        slider.addPoof(new Slider.SliderPoof<Slider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(Slider component, SliderPoofInfo info) {
                                staticSetting.setValue(info.getNewValue());
                            }
                        });
                        addChild(slider);
                    }
                }else if(isBoolean) {
                    CheckButton checkButton = new CheckButton(staticSetting.getDisplayName());
                    checkButton.setToggled((Boolean) staticSetting.getValue());
                    checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                        @Override
                        public void execute(CheckButton checkButton1, CheckButtonPoofInfo info) {
                            if (info.getAction() == CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE)
                                staticSetting.setValue(checkButton.isToggled());
                        }
                    });
                    addChild(checkButton);
                }else if(isEnum) {
                    Object[] con = type.getEnumConstants();
                    String[] modes = Arrays.stream(con).map(o -> o.toString().toUpperCase()).toArray(String[]::new);
                    EnumButton enumbutton = new EnumButton(staticSetting.getDisplayName(), modes);
                    enumbutton.addPoof(new EnumButton.EnumbuttonIndexPoof<EnumButton, EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo>() {
                        @Override
                        public void execute(EnumButton component, EnumbuttonInfo info) {
                            staticSetting.setValue(con[info.getNewIndex()]);
                        }
                    });
                    enumbutton.setIndex(Arrays.asList(con).indexOf(staticSetting.getValue()));
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
