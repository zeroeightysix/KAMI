package me.zeroeightsix.kami.feature.module.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.render.entity.PlayerModelPart;

import java.util.Random;

/**
 * Created by 086 on 30/01/2018.
 */
@Module.Info(name = "SkinFlicker", description = "Toggle the jacket layer rapidly for a cool skin effect", category = Module.Category.MISC)
public class SkinFlicker extends Module {

    private Setting<FlickerMode> mode = register(Settings.e("Mode", FlickerMode.HORIZONTAL));
    private Setting<Integer> slowness = register(Settings.integerBuilder().withName("Slowness").withValue(2).withMinimum(1).build());

    private final static PlayerModelPart[] PARTS_HORIZONTAL = new PlayerModelPart[]{
            PlayerModelPart.LEFT_SLEEVE,
            PlayerModelPart.JACKET,
            PlayerModelPart.HAT,
            PlayerModelPart.LEFT_PANTS_LEG,
            PlayerModelPart.RIGHT_PANTS_LEG,
            PlayerModelPart.RIGHT_SLEEVE
    };

    private final static PlayerModelPart[] PARTS_VERTICAL = new PlayerModelPart[]{
            PlayerModelPart.HAT,
            PlayerModelPart.JACKET,
            PlayerModelPart.LEFT_SLEEVE,
            PlayerModelPart.RIGHT_SLEEVE,
            PlayerModelPart.LEFT_PANTS_LEG,
            PlayerModelPart.RIGHT_PANTS_LEG,
    };

    private Random r = new Random();
    private int len = PlayerModelPart.values().length;

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        switch (mode.getValue()) {
            case RANDOM:
                if (mc.player.age % slowness.getValue() != 0) return;
                mc.options.togglePlayerModelPart(PlayerModelPart.values()[r.nextInt(len)]);
                break;
            case VERTICAL:
            case HORIZONTAL:
                int i = (mc.player.age / slowness.getValue()) % (PARTS_HORIZONTAL.length * 2); // *2 for on/off
                boolean on = false;
                if (i >= PARTS_HORIZONTAL.length) {
                    on = true;
                    i -= PARTS_HORIZONTAL.length;
                }
                mc.options.setPlayerModelPart(mode.getValue() == FlickerMode.VERTICAL ? PARTS_VERTICAL[i] : PARTS_HORIZONTAL[i], on);
        }
    });

    public enum FlickerMode {
        HORIZONTAL, VERTICAL, RANDOM
    }

}
