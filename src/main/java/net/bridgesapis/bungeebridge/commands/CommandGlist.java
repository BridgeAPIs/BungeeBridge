package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

import java.util.HashMap;

public class CommandGlist extends Command {
	private final BungeeBridge bridge;

	public CommandGlist(BungeeBridge bridge) {
		super("glist", "proxies.glist");
		this.bridge = bridge;
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		HashMap<String, Long> proxiesSlots = new HashMap<>();
		HashMap<String, Long> serversSlots = new HashMap<>();
		int total = 0;

		Jedis cache = bridge.getConnector().getBungeeResource();
		for (String proxy : bridge.getNetworkBridge().getProxies()) {
			long value = cache.scard("connected:" + proxy);
			proxiesSlots.put(proxy, value);
			total += value;
		}

		for (String server : bridge.getServersManager().getServers()) {
			long value = cache.scard("connectedonserv:" + server);
			if (value > 0)
				serversSlots.put(server, value);
		}

		TextComponent ctotal = new TextComponent("Nombre de joueurs total : ");
		ctotal.setColor(ChatColor.YELLOW);
		TextComponent totalPlayers = new TextComponent(total + " joueurs");
		totalPlayers.setColor(ChatColor.GREEN);
		ctotal.addExtra(" ");
		ctotal.addExtra(totalPlayers);

		commandSender.sendMessage(ctotal);

		// Par proxy
		commandSender.sendMessage(new ComponentBuilder("Joueurs par proxy : ").color(ChatColor.YELLOW).create());
	    for (String name : proxiesSlots.keySet()) {
			TextComponent line = new TextComponent(" - " + name + " : " + proxiesSlots.get(name) + " joueurs");
			line.setColor(ChatColor.GREEN);
			commandSender.sendMessage(line);
		}

		// Par serveur
		TextComponent byServ = new TextComponent("Joueurs par serveur : ");
		byServ.setColor(ChatColor.YELLOW);
		for (String name : serversSlots.keySet()) {
			TextComponent line = new TextComponent("[" + name + " : ");
			line.setColor(ChatColor.AQUA);
			TextComponent val = new TextComponent("" + serversSlots.get(name));
			val.setColor(ChatColor.GREEN);
			TextComponent closing = new TextComponent("]");
			closing.setColor(ChatColor.GREEN);

			line.addExtra(val);
			line.addExtra(closing);
			byServ.addExtra(line);
		}

		commandSender.sendMessage(byServ);
	}
}
