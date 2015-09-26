package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class CommandDelAutoMessage extends Command {

	public CommandDelAutoMessage() {
		super("delautomessage", "automessage.del", "dam");
	}

	@Override
	public void execute(CommandSender arg0, String[] args) {
		
		ProxiedPlayer p = (ProxiedPlayer) arg0;
		if (!p.hasPermission("automessage.del")) {
			TextComponent c = new TextComponent(I18n.getTranslation("permission_denied"));
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else {
			Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
			jedis.del("automessage");
			jedis.close();

			TextComponent c = new TextComponent(I18n.getTranslation("automessage_removed"));
			c.setColor(ChatColor.GREEN);
			p.sendMessage(c);
		}
	}

}
