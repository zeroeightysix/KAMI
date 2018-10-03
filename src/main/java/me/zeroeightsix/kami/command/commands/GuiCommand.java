package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.jtcui.Fat;
import me.zeroeightsix.jtcui.JTCBuilder;
import me.zeroeightsix.jtcui.component.Pane;
import me.zeroeightsix.jtcui.layout.layouts.FixedSelfSizingLayout;
import me.zeroeightsix.jtcui.layout.layouts.SelfSizingLayout;
import me.zeroeightsix.jtcui.layout.layouts.VerticalLayout;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.gui.KamiJTCRenderHandler;
import me.zeroeightsix.kami.gui.Window;

import static me.zeroeightsix.kami.KamiMod.jtc;

/**
 * Created by 086 on 3/10/2018.
 */
public class GuiCommand extends Command {

    public GuiCommand() {
        super("grel", SyntaxChunk.EMPTY);
    }

    @Override
    public void call(String[] args) {
        setupJTC();
    }

    public static void setupJTC() {
        jtc = JTCBuilder.builder(new KamiJTCRenderHandler()).build();

        Pane pane = new Pane(new FixedSelfSizingLayout(SelfSizingLayout.Type.EXPANDING), new Fat(20, 20, 20, 20));
        Window window = new Window(5, 5, 100, 100, "Test Window", 20, 4);
        window.setLayout(new VerticalLayout(SelfSizingLayout.Type.EXPANDING));
        pane.getChildren().add(window);
        jtc.getRootComponent().getChildren().add(pane);
    }

}
