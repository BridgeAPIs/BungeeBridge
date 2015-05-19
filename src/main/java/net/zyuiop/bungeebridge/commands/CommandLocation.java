package net.zyuiop.bungeebridge.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.players.PlayerData;

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
			commandSender.sendMessage(new ComponentBuilder("Utilisation : /locate <pseudo>").color(ChatColor.RED).create());
		} else {
			UUID id = plugin.getUuidTranslator().getUUID(strings[0], false);
			if (id == null) {
				commandSender.sendMessage(new ComponentBuilder("Ce pseudo est invalide.").color(ChatColor.RED).create());
			} else if (!plugin.getNetworkBridge().isOnline(id)) {
				commandSender.sendMessage(new ComponentBuilder("Ce joueur n'est pas en ligne.").color(ChatColor.RED).create());
			} else {
				PlayerData data = plugin.getPlayerDataManager().getPlayerData(id);
				String proxy = data.get("currentproxy", "Inconnu");
				String server = data.get("currentserver", "Inconnu");
				long lastaction = data.getLong("lastaction", 0);
				Date last = new Date(lastaction);

				commandSender.sendMessage(new ComponentBuilder("#---------[ Joueur " + strings[0] + " ]---------#").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder("Proxy actuel : ").color(ChatColor.YELLOW).append(proxy).color(ChatColor.GREEN).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder("Serveur actuel : ").color(ChatColor.YELLOW).append(server).color(ChatColor.GREEN).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder("Dernier signe de vie : ").color(ChatColor.YELLOW).append(last + "").color(ChatColor.GREEN).create());
				commandSender.sendMessage(new ComponentBuilder(" ").color(ChatColor.YELLOW).create());
				commandSender.sendMessage(new ComponentBuilder("#---------[ Joueur " + strings[0] + " ]---------#").color(ChatColor.YELLOW).create());

			}
		}
	}


}
