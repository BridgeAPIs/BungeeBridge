package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.players.PlayerData;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.UUID;

public class CommandLocation extends DefaultExecutor {

	private final BungeeBridge plugin;

	public CommandLocation(BungeeBridge plugin) {
		super("locate", "proxies.locate");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if (strings.length < 1) {
			commandSender.sendMessage(new ComponentBuilder("Usage : /locate <pseudo>").color(ChatColor.RED).create());
		} else {
			UUID id = plugin.getUuidTranslator().getUUID(strings[0], false);
			if (id == null) {
				commandSender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
			} else if (!plugin.getNetworkBridge().isOnline(id)) {
				commandSender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.offline")).color(ChatColor.RED).create());
			} else {
				PlayerData data = plugin.getPlayerDataManager().getPlayerData(id);
				String proxy = data.get("currentproxy", StringUtils.capitalize(I18n.getWord("unknown")));
				String server = data.get("currentserver", StringUtils.capitalize(I18n.getWord("unknown")));
				long lastaction = data.getLong("lastaction", 0);
				Date last = new Date(lastaction);

				commandSender.sendMessage(new ComponentBuilder("#---------[ " + StringUtils.capitalize(I18n.getWord("player")) + " " + strings[0] + " ]---------#").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.current_proxy")).color(ChatColor.YELLOW).append(proxy).color(ChatColor.GREEN).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.current_server")).color(ChatColor.YELLOW).append(server).color(ChatColor.GREEN).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.last_lifesign")).color(ChatColor.YELLOW).append(last + "").color(ChatColor.GREEN).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder("#---------[ " + StringUtils.capitalize(I18n.getWord("player")) + " " + strings[0] + " ]---------#").color(ChatColor.YELLOW).create());

			}
		}
	}


}
