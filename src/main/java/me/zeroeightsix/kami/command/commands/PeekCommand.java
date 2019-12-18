package me.zeroeightsix.kami.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.mixin.client.IShulkerBoxBlockEntity;
import me.zeroeightsix.kami.util.ShulkerBoxCommon;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.container.ShulkerBoxContainer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

/**
 * @author 086
 */
public class PeekCommand extends Command {

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType(o -> new LiteralText(o.toString()));
    private static boolean subscribed = false;

    public ShulkerBoxBlockEntity sb;

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("peek").executes(context -> {
            ItemStack is = Wrapper.getPlayer().inventory.getMainHandStack();

            if (ShulkerBoxCommon.isShulkerBox(is.getItem())) {
                ShulkerBoxBlockEntity entityBox = new ShulkerBoxBlockEntity(((ShulkerBoxBlock) ((BlockItem) is.getItem()).getBlock()).getColor());
                entityBox.setWorld(Wrapper.getWorld());
                CompoundTag tag = is.getSubTag("BlockEntityTag");
                if (tag != null) {
                    entityBox.fromTag(tag);
                    this.sb = entityBox;
                    if (!subscribed) {
                        KamiMod.EVENT_BUS.subscribe(this);
                        subscribed = true;
                    }
                } else {
                    throw FAILED_EXCEPTION.create("Couldn't peek into shulker box. It might be empty.");
                }
            } else {
                throw FAILED_EXCEPTION.create("You must be holding a shulker box to peek into.");
            }
            return 0;
        }));
    }

    @EventHandler
    public Listener<TickEvent.Client> tickListener = new Listener<>(event -> {
        if (this.sb != null) {
            ShulkerBoxContainer container = (ShulkerBoxContainer) ((IShulkerBoxBlockEntity) this.sb).invokeCreateContainer(-1, Wrapper.getPlayer().inventory);
            ShulkerBoxScreen gui = new ShulkerBoxScreen(container, Wrapper.getPlayer().inventory, this.sb.getDisplayName());
            MinecraftClient.getInstance().openScreen(gui);
            this.sb = null;
        }
    }); // TODO: Find a way to unsubscribe this listener safely

}
