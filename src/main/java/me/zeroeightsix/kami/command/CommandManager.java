package me.zeroeightsix.kami.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;
import org.reflections.Reflections;

import java.util.ArrayList;

public class CommandManager {
	
	private ArrayList<Command> commands = new ArrayList<>();
	public CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

	public void generateCommands() {
		this.commands.clear();
		discoverCommands();
		populateDispatcher(dispatcher = new CommandDispatcher<>());
	}

	private void populateDispatcher(CommandDispatcher<CommandSource> dispatcher) {
		for (Command command : commands) {
			command.register(dispatcher);
		}
	}

	private void discoverCommands() {
		Reflections reflections = new Reflections();
		reflections.getSubTypesOf(Command.class).forEach(s -> {
			if (Command.class.isAssignableFrom(s)){
				try {
					Command command = (Command) s.getDeclaredField("INSTANCE").get(null);
					commands.add(command);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Couldn't initiate command " + s.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
				}
			}
		});
	}

	public ArrayList<Command> getCommands() {
		return commands;
	}
	
}
