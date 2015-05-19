package net.zyuiop.bungeebridge.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.zyuiop.bungeebridge.BungeeBridge;
import redis.clients.jedis.Jedis;

public class CommandDelAutoMessage extends Command {

	public CommandDelAutoMessage() {
		super("delautomessage", "automessage.del", "dam");
	}

	@Override
	public void execute(CommandSender arg0, String[] args) {
		
		ProxiedPlayer p = (ProxiedPlayer) arg0;
		if (!p.hasPermission("automessage.del")) {
			TextComponent c = new TextComponent("Vous n'avez pas la permission.");
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else {
			Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
			jedis.del("automessage");
			jedis.close();

			TextComponent c = new TextComponent("Message automatique supprimé.");
			c.setColor(ChatColor.GREEN);
			p.sendMessage(c);
		}
	}

}
