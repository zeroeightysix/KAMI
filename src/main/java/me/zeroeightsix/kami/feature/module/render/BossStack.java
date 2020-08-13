package me.zeroeightsix.kami.feature.module.render;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.RenderBossBarEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.SettingVisibility;
import net.minecraft.client.gui.hud.ClientBossBar;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by 086 on 25/01/2018.
 */
@Module.Info(name = "BossStack", description = "Modify the boss health GUI to take up less space", category = Module.Category.MISC)
public class BossStack extends Module {

    @Setting(name = "Hide boss bars")
    private boolean remove = false;
    @Setting
    @SettingVisibility.Method("ifNotRemove")
    private boolean fold = true;
    @Setting
    @SettingVisibility.Method("ifNotRemoveNotFold")
    private int spacing = 10;

    public boolean ifNotRemove() {
        return !remove;
    }

    public boolean ifNotRemoveNotFold() {
        return !remove && !fold;
    }

    public static final WeakHashMap<ClientBossBar, Integer> barMap = new WeakHashMap<>();

    @EventHandler
    private Listener<RenderBossBarEvent.GetIterator> getIteratorListener = new Listener<>(event -> {
        if (remove) {
            event.cancel();
            return;
        }

        if (fold) {
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
    private Listener<RenderBossBarEvent.Spacing> spacingListener = new Listener<>(event -> event.setSpacing(event.getSpacing() - spacing));


}
