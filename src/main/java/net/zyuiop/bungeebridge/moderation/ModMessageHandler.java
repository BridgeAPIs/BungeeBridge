package net.zyuiop.bungeebridge.moderation;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.zyuiop.bungeebridge.core.handlers.PubSubConsumer;

public class ModMessageHandler implements PubSubConsumer {
	@Override
	public void consume(String channel, String message) {
		JsonModMessage modMessage = new Gson().fromJson(message, JsonModMessage.class);

		TextComponent premsg = new TextComponent("(Modération : ");
		premsg.setColor(ChatColor.DARK_AQUA);

		TextComponent sender = new TextComponent(modMessage.getSender());
		if (modMessage.getSenderPrefix() == null) {
			sender.setColor(ChatColor.DARK_AQUA);
		} else {
			sender.setColor(modMessage.getSenderPrefix());
		}

		TextComponent prefixEnd = new TextComponent(") : ");
		prefixEnd.setColor(ChatColor.DARK_AQUA);

		TextComponent msg = new TextComponent(modMessage.getMessage());
		msg.setColor(ChatColor.WHITE);

		premsg.addExtra(sender);
		premsg.addExtra(prefixEnd);
		premsg.addExtra(msg);

		ProxyServer.getInstance().getPlayers().stream().filter(p -> p.hasPermission("modo.modchan")).forEach(p -> p.sendMessage(premsg));
	}
}
