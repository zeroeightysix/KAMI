package me.zeroeightsix.kami.command;

import com.mojang.brigadier.CommandDispatcher;
import me.zeroeightsix.kami.command.commands.PluginCommand;
import me.zeroeightsix.kami.command.commands.ToggleCommand;
import me.zeroeightsix.kami.util.ClassFinder;
import net.minecraft.server.command.CommandSource;

import java.util.ArrayList;
import java.util.Set;

public class CommandManager {
	
	private ArrayList<Command> commands = new ArrayList<>();
	public CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

	public void generateCommands() {
		this.commands.clear();
		discoverCommands();
		commands.add(PluginCommand.INSTANCE); // Kotlin objects don't have constructors
		populateDispatcher(dispatcher = new CommandDispatcher<>());
	}

	private void populateDispatcher(CommandDispatcher<CommandSource> dispatcher) {
		for (Command command : commands) {
			command.register(dispatcher);
		}
	}

	private void discoverCommands() {
		Set<Class> classList = ClassFinder.findClasses(ToggleCommand.class.getPackage().getName(), Command.class);
		for (Class s : classList) {
			if (Command.class.isAssignableFrom(s)){
				try {
					Command command = (Command) s.getConstructor().newInstance();
					commands.add(command);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Couldn't initiate command " + s.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
				}
			}
		}
	}

	public ArrayList<Command> getCommands() {
		return commands;
	}
	
}
