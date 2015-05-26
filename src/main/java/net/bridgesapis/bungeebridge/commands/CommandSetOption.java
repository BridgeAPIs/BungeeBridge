package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.database.ServerSettings;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CommandSetOption extends DefaultExecutor {

	private final BungeeBridge plugin;

	public CommandSetOption(BungeeBridge plugin) {
		super("setoption", "proxies.setoption", "spo");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if (strings.length < 2) {
			commandSender.sendMessage(new ComponentBuilder("Usage : /setoption <option> <value>").color(ChatColor.RED).create());
		} else {
			String option = strings[0];
			String value = StringUtils.join(Arrays.copyOfRange(strings, 1, strings.length), " ");

			ServerSettings serverSettings = plugin.getServerSettings();
			switch (option) {
				case "motd":
					value = ChatColor.translateAlternateColorCodes('&', value);
					serverSettings.setMotd(value);
					break;
				case "serverline":
					value = ChatColor.translateAlternateColorCodes('&', value);
					serverSettings.setServerLine(value);
					break;
				case "slots":
					serverSettings.setMaxPlayers(Integer.valueOf(value));
					break;
				case "state":
					serverSettings.setType(ServerSettings.CloseType.fromString(value));
					break;
				default:
					commandSender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.setoption.unknown")).color(ChatColor.RED).create());
					return;
			}

			commandSender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.setoption.defined")).color(ChatColor.GREEN).create());

		}
	}


}
