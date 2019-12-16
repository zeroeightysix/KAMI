package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisconnectedScreen.class)
public interface IDisconnectedScreen {

    @Accessor
    Screen getParent();
    @Accessor
    Text getReason();

}
