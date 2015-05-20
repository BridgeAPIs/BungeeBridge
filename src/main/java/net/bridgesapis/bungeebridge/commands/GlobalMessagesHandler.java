package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;

public class GlobalMessagesHandler implements PubSubConsumer {
	@Override
	public void consume(String channel, String message) {
		String sentBy = StringUtils.split(channel, ".")[1];

		TextComponent sender = new TextComponent(ChatColor.DARK_RED +   "[" + ChatColor.RED + "" + ChatColor.BOLD + "Annonce" + ChatColor.DARK_RED + "] " + ChatColor.RED + ChatColor.BOLD + sentBy +" : ");
		TextComponent msg = new TextComponent(message);
		msg.setColor(ChatColor.GOLD);
		msg.setBold(true);
		sender.addExtra(msg);
		ProxyServer.getInstance().broadcast(sender);
	}
}
