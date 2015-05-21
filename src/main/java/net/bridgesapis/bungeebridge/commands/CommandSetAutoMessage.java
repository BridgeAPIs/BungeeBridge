package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

public class CommandSetAutoMessage extends Command {

	public CommandSetAutoMessage() {
		super("setautomessage", "automessage.set", "sam");
	}

	@Override
	public void execute(CommandSender arg0, String[] args) {
		
		ProxiedPlayer p = (ProxiedPlayer) arg0;
		if (!p.hasPermission("automessage.set")) {
			TextComponent c = new TextComponent(I18n.getTranslation("permission_denied"));
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else if (args.length < 2) {
			TextComponent c = new TextComponent("/setautomessage [durée] [message]");
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else {
			String[] end = args[0].split(":");
			int time = 0;
			switch (end[1]) {
				case "d":
					time += Integer.parseInt(end[0]) * 60 * 60 * 24;
					break;
				case "h":
					time += Integer.parseInt(end[0]) * 60 * 60;
					break;
				case "m":
					time += Integer.parseInt(end[0]) * 60;
					break;
				case "s":
					time += Integer.parseInt(end[0]);
					break;
			}

			String[] phrases = Arrays.copyOfRange(args, 1, args.length);

			Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
			jedis.set("automessage", StringUtils.join(phrases, " "));
			jedis.expire("automessage", time);
			jedis.close();

			TextComponent c = new TextComponent("Message automatique définit.");
			c.setColor(ChatColor.GREEN);
			p.sendMessage(c);
		}
	}

}
