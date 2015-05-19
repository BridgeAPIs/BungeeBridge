package net.zyuiop.bungeebridge.interactions.parties;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.core.handlers.PubSubConsumer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.UUID;

public class PartiesConsumer implements PubSubConsumer {

	private final PartiesManager partiesManager;

	public PartiesConsumer(PartiesManager partiesManager) {
		this.partiesManager = partiesManager;
	}

	@Override
	public void consume(String channel, String message) {
		String[] parts = StringUtils.split(channel, ".");
		if (parts.length < 2)
			return;

		String action = parts[1];
		String[] args = StringUtils.split(message, " ");

		if (action.equals("invite")) {
			if (args.length < 3)
				return;

			UUID from = UUID.fromString(args[0]);
			String name = args[1];
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(args[2]));

			if (player == null)
				return;

			partiesManager.invite(from, player.getUniqueId());

			TextComponent disconnect = new TextComponent("[Party]");
			disconnect.setColor(ChatColor.DARK_GREEN);
			TextComponent n = new TextComponent(name + " vous invite à rejoindre sa party. Vous avez 60 secondes pour accepter.");
			n.setColor(ChatColor.YELLOW);
			TextComponent accept = new TextComponent("[Accepter]");
			accept.setColor(ChatColor.GREEN);
			accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + name));

			disconnect.addExtra(" ");
			disconnect.addExtra(n);
			disconnect.addExtra(" ");
			disconnect.addExtra(accept);

			player.sendMessage(disconnect);
		} else if (action.equals("join")) {
			if (args.length < 3)
				return;

			UUID party = UUID.fromString(args[0]);
			UUID player = UUID.fromString(args[1]);
			String name = args[2];
			TextComponent disconnect = new TextComponent("[Party]");
			disconnect.setColor(ChatColor.DARK_GREEN);
			TextComponent n = new TextComponent(name);
			n.setColor(ChatColor.YELLOW);
			TextComponent left = new TextComponent("a rejoint la party.");
			left.setColor(ChatColor.GREEN);

			disconnect.addExtra(" ");
			disconnect.addExtra(n);
			disconnect.addExtra(" ");
			disconnect.addExtra(left);

			partiesManager.getPlayersInParty(party).keySet().stream().filter(member -> ProxyServer.getInstance().getPlayer(member) != null).forEach(member -> ProxyServer.getInstance().getPlayer(member).sendMessage(disconnect));

		} else if (action.equals("kick")) {
			if (args.length < 3)
				return;

			UUID party = UUID.fromString(args[0]);
			UUID player = UUID.fromString(args[1]);
			String name = args[2];
			TextComponent disconnect = new TextComponent("[Party]");
			disconnect.setColor(ChatColor.DARK_GREEN);
			TextComponent n = new TextComponent(name);
			n.setColor(ChatColor.YELLOW);
			TextComponent left = new TextComponent("a été exclus.");
			left.setColor(ChatColor.RED);

			disconnect.addExtra(" ");
			disconnect.addExtra(n);
			disconnect.addExtra(" ");
			disconnect.addExtra(left);

			partiesManager.getPlayersInParty(party).keySet().stream().filter(member -> ProxyServer.getInstance().getPlayer(member) != null).forEach(member -> ProxyServer.getInstance().getPlayer(member).sendMessage(disconnect));

		} else if (action.equals("leave")) {
			if (args.length < 3)
				return;

			UUID party = UUID.fromString(args[0]);
			UUID player = UUID.fromString(args[1]);
			String name = args[2];
			TextComponent disconnect = new TextComponent("[Party]");
			disconnect.setColor(ChatColor.DARK_GREEN);
			TextComponent n = new TextComponent(name);
			n.setColor(ChatColor.YELLOW);
			TextComponent left = new TextComponent("est parti.");
			left.setColor(ChatColor.GREEN);

			disconnect.addExtra(" ");
			disconnect.addExtra(n);
			disconnect.addExtra(" ");
			disconnect.addExtra(left);

			partiesManager.getPlayersInParty(party).keySet().stream().filter(member -> ProxyServer.getInstance().getPlayer(member) != null).forEach(member -> ProxyServer.getInstance().getPlayer(member).sendMessage(disconnect));

		} else if (action.equals("lead")) {
			if (args.length < 2)
				return;

			UUID party = UUID.fromString(args[0]);
			String name = args[1];
			TextComponent disconnect = new TextComponent("[Party]");
			disconnect.setColor(ChatColor.DARK_GREEN);
			TextComponent n = new TextComponent(name);
			n.setColor(ChatColor.YELLOW);
			TextComponent left = new TextComponent("est maintenant chef de groupe.");
			left.setColor(ChatColor.GREEN);

			disconnect.addExtra(" ");
			disconnect.addExtra(n);
			disconnect.addExtra(" ");
			disconnect.addExtra(left);

			partiesManager.getPlayersInParty(party).keySet().stream().filter(member -> ProxyServer.getInstance().getPlayer(member) != null).forEach(member -> ProxyServer.getInstance().getPlayer(member).sendMessage(disconnect));

		} else if (action.equals("disconnect")) {
			if (args.length < 2)
				return;

			UUID party = UUID.fromString(args[0]);
			String name = args[1];
			TextComponent disconnect = new TextComponent("[Party]");
			disconnect.setColor(ChatColor.DARK_GREEN);
			TextComponent n = new TextComponent(name);
			n.setColor(ChatColor.YELLOW);
			TextComponent left = new TextComponent("s'est déconnecté. Il a 5 minutes pour revenir avant d'être exclus du groupe.");
			left.setColor(ChatColor.GREEN);

			disconnect.addExtra(" ");
			disconnect.addExtra(n);
			disconnect.addExtra(" ");
			disconnect.addExtra(left);

			partiesManager.getPlayersInParty(party).keySet().stream().filter(member -> ProxyServer.getInstance().getPlayer(member) != null).forEach(member -> ProxyServer.getInstance().getPlayer(member).sendMessage(disconnect));

		}  else if (action.equals("comeback")) {
			if (args.length < 2)
				return;

			UUID party = UUID.fromString(args[0]);
			String name = args[1];
			TextComponent disconnect = new TextComponent("[Party]");
			disconnect.setColor(ChatColor.DARK_GREEN);
			TextComponent n = new TextComponent(name);
			n.setColor(ChatColor.YELLOW);
			TextComponent left = new TextComponent("s'est reconnecté.");
			left.setColor(ChatColor.GREEN);

			disconnect.addExtra(" ");
			disconnect.addExtra(n);
			disconnect.addExtra(" ");
			disconnect.addExtra(left);

			partiesManager.getPlayersInParty(party).keySet().stream().filter(member -> ProxyServer.getInstance().getPlayer(member) != null).forEach(member -> ProxyServer.getInstance().getPlayer(member).sendMessage(disconnect));

		} else if (action.equals("disband")) {
			if (args.length < 1)
				return;

			UUID player = UUID.fromString(args[0]);
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
			if (p != null)
				p.sendMessage(new ComponentBuilder("[Party]").color(ChatColor.DARK_GREEN).append(" Votre partie a été dissoute.").color(ChatColor.RED).create());
		} else if (action.equals("message")) {
			if (args.length < 1)
				return;

			UUID party = UUID.fromString(args[0]);
			String sender = args[1];
			String msg = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
			for (UUID player : partiesManager.getPlayersInParty(party).keySet()) {
				ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
				if (p != null)
					p.sendMessage(new ComponentBuilder("[Party] " + sender + " : ").color(ChatColor.DARK_GREEN).append(msg).color(ChatColor.GREEN).create());
			}
		}
	}
}
