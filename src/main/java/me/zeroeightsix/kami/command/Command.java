package me.zeroeightsix.kami.command;

import com.mojang.brigadier.CommandDispatcher;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command {
	
	public static char SECTION_SIGN = '\u00A7';
	public static Setting<Character> commandPrefix = Settings.c("commandPrefix", '.');

	public abstract void register(CommandDispatcher<CommandSource> dispatcher);

	public static void sendChatMessage(String message){
		sendRawChatMessage("&7[&a" + KamiMod.KAMI_KANJI + "&7] &r" + message);
	}

	public static void sendRawChatMessage(String message){
		Wrapper.getPlayer().sendMessage(new ChatMessage(message));
	}

	public static char getCommandPrefix() {
		return commandPrefix.getValue();
	}
	
    public static class ChatMessage extends LiteralText {

		String text;
		
		public ChatMessage(String text) {
			super(text);

			Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
			Matcher m = p.matcher(text);
			StringBuffer sb = new StringBuffer();

			while (m.find()) {
			    String replacement = "\u00A7" + m.group().substring(1);
			    m.appendReplacement(sb, replacement);
			}

			m.appendTail(sb);

			this.text = sb.toString();
		}
		
		public String getUnformattedComponentText() {
			return text;
		}

		@Override
		public Text copy() {
			return new ChatMessage(text);
		}

	}

}
