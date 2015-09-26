package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;

public class GlobalMessagesHandler implements PubSubConsumer {
	@Override
	public void consume(String channel, String message) {
		String sentBy = StringUtils.split(channel, ".")[1];

		TextComponent sender = new TextComponent(I18n.getTranslation("global_prefix").replace("%NAME%", sentBy).replace("%MESSAGE%", message));
		ProxyServer.getInstance().broadcast(sender);
	}
}
