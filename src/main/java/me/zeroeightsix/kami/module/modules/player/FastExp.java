package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module.Info;
import net.minecraft.init.Items;

@Module.Info(name = "FastExp", category = Module.Category.PLAYER, description = "Removes the delay when throwing Exp bottles")
public class FastExp
extends Module
{
	@EventHandler
	private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event ->
	{
		if (mc.player != null && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE)) {

			mc.rightClickDelayTimer = 0;
		}
		else {
			mc.player.getHeldItemMainhand().getItem()
		}
	}
}
