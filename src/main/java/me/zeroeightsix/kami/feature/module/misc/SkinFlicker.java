package me.zeroeightsix.kami.feature.module.misc;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;

import java.util.Random;

@Module.Info(name = "SkinFlicker", description = "Toggle the jacket layer rapidly for a cool skin effect", category = Module.Category.MISC)
public class SkinFlicker extends Module {

    @Setting(name = "Mode")
    private FlickerMode mode = FlickerMode.HORIZONTAL;
    @Setting(name = "Slowness")
    private @Setting.Constrain.Range(min = 1) int slowness = 2;

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
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        ClientPlayerEntity player = event.getPlayer();

        switch (mode) {
            case RANDOM:
                if (player.age % slowness != 0) return;
                mc.options.togglePlayerModelPart(PlayerModelPart.values()[r.nextInt(len)]);
                break;
            case VERTICAL:
            case HORIZONTAL:
                int i = (player.age / slowness) % (PARTS_HORIZONTAL.length * 2); // *2 for on/off
                boolean on = false;
                if (i >= PARTS_HORIZONTAL.length) {
                    on = true;
                    i -= PARTS_HORIZONTAL.length;
                }
                mc.options.setPlayerModelPart(mode == FlickerMode.VERTICAL ? PARTS_VERTICAL[i] : PARTS_HORIZONTAL[i], on);
        }
    });

    public enum FlickerMode {
        HORIZONTAL, VERTICAL, RANDOM
    }

}
