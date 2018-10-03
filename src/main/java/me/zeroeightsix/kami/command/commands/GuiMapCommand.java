package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.jtcui.component.Component;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by 086 on 3/10/2018.
 */
public class GuiMapCommand extends Command {

    public GuiMapCommand() {
        super("gmap", SyntaxChunk.EMPTY);
    }

    @Override
    public void call(String[] args) {
        map(KamiMod.jtc.getRootComponent());
    }

    private void map(Component component) {
        mapRec("", component);
    }

    private void mapRec(String prefix, Component component) {
        Command.sendChatMessage(prefix + component.getClass().getSimpleName() + " " + component.getSpace().toString());
        Optional.ofNullable(component.getChildren()).ifPresent(components -> {
            final String p = String.join("", Collections.nCopies(prefix.length(), " ")) + "├ ";
            components.forEach(component1 -> mapRec(p, component1));
        });
    }

}
