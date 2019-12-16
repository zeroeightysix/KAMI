package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;

public class TestCommand extends Command {

    public static boolean modifiersEnabled = true;

    public TestCommand() {
        super("test");
    }

    @Override
    public void call(String[] args) {

    }
}
