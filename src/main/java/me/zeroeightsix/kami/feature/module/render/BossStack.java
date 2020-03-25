package me.zeroeightsix.kami.feature.module.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.RenderBossBarEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.hud.ClientBossBar;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by 086 on 25/01/2018.
 */
@Module.Info(name = "BossStack", description = "Modify the boss health GUI to take up less space", category = Module.Category.MISC)
public class BossStack extends Module {

    private Setting<Boolean> remove = register(Settings.b("Hide boss bars", false));
    private Setting<Boolean> fold = register(Settings.booleanBuilder("Fold").withVisibility(b -> !remove.getValue()).withValue(true).build());
    private Setting<Integer> spacing = register(Settings.integerBuilder("Spacing offset").withVisibility(d -> !remove.getValue()).withValue(0).build());

    public static final WeakHashMap<ClientBossBar, Integer> barMap = new WeakHashMap<>();

    @EventHandler
    private Listener<RenderBossBarEvent.GetIterator> getIteratorListener = new Listener<>(event -> {
        if (remove.getValue()) {
            event.cancel();
            return;
        }

        if (fold.getValue()) {
            HashMap<String, ClientBossBar> chosenBarMap = new HashMap<>();
            event.getIterator().forEachRemaining(bar -> {
                String name = bar.getName().asString();
                if (chosenBarMap.containsKey(name)) {
                    barMap.compute(chosenBarMap.get(name), (clientBossBar, integer) -> (integer == null) ? 2 : integer + 1);
                } else {
                    chosenBarMap.put(name, bar);
                }
            });
            event.setIterator(chosenBarMap.values().iterator());
        }
    });

    @EventHandler
    private Listener<RenderBossBarEvent.GetText> getTextListener = new Listener<>(event -> {
        if (BossStack.barMap.isEmpty()) return;
        ClientBossBar bar = event.getBossBar();
        Integer integer = barMap.get(bar);
        barMap.remove(bar);
        if (integer != null) {
            event.setText(event.getText().copy().append(" x" + integer));
        }
    });

    @EventHandler
    private Listener<RenderBossBarEvent.Spacing> spacingListener = new Listener<>(event -> {
        event.setSpacing(event.getSpacing() - spacing.getValue());
    });

}
