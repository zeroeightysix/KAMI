package me.zeroeightsix.kami.feature.module.render;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.SettingVisibility;

import java.util.Stack;
import java.util.function.Function;

/**
 * Created by 086 on 12/12/2017.
 * @see me.zeroeightsix.kami.mixin.client.MixinEntityRenderer
 */
@Module.Info(name = "Brightness", description = "Makes everything brighter!", category = Module.Category.RENDER)
public class Brightness extends Module {

    @Setting(name = "Transition")
    private boolean transition = true;
    @Setting(name = "Seconds")
    @SettingVisibility.Method("ifTransition")
    private @Setting.Constrain.Range(min = 0, max = 10) float seconds = 1;
    @Setting(name = "Mode")
    private Transition mode = Transition.SINE;
    
    public boolean ifTransition() {
        return transition;
    }

    private Stack<Float> transitionStack = new Stack<>();

    private static float currentBrightness = 0;
    private static boolean inTransition = false;

    private void addTransition(boolean isUpwards) {
        if (transition) {
            int length = (int) (seconds * 20);
            float[] values;
            switch (mode) {
                case LINEAR:
                    values = linear(length, isUpwards);
                    break;
                case SINE:
                    values = sine(length, isUpwards);
                    break;
                default:
                    values = new float[]{0};
                    break;
            }
            for (float v : values) {
                transitionStack.add(v);
            }

            inTransition = true;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        addTransition(true);
    }

    @Override
    public void onDisable() {
        setAlwaysListening(true);
        super.onDisable();
        addTransition(false);
    }

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (inTransition) {
            if (transitionStack.isEmpty()) {
                inTransition = false;
                setAlwaysListening(false);
                currentBrightness = isEnabled() ? 1 : 0;
            } else {
                currentBrightness = transitionStack.pop();
            }
        }
    });

    private float[] createTransition(int length, boolean upwards, Function<Float, Float> function) {
        float[] transition = new float[length];
        for (int i = 0; i < length; i++) {
            float v = function.apply(((float) i / (float) length));
            if (upwards) v = 1 - v;
            transition[i] = v;
        }
        return transition;
    }

    private float[] linear(int length, boolean polarity) { // length of 20 = 1 second
        return createTransition(length, polarity, d -> d);
    }

    private float sine(float x) { // (sin(pi*x-(pi/2)) + 1) / 2
        return ((float) Math.sin(Math.PI * x - Math.PI / 2) + 1) / 2;
    }

    private float[] sine(int length, boolean polarity) {
        return createTransition(length, polarity, this::sine);
    }

    public static float getCurrentBrightness() {
        return currentBrightness;
    }

    public static boolean isInTransition() {
        return inTransition;
    }

    public static boolean shouldBeActive() {
        return isInTransition() || currentBrightness == 1; // if in transition or enabled
    }

    public enum Transition {
        LINEAR, SINE

    }
}
