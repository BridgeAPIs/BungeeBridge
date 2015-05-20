package net.bridgesapis.bungeebridge.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.database.Publisher;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CommandGlobal extends Command {

	private final BungeeBridge plugin;

	public CommandGlobal(BungeeBridge plugin) {
		super("global", "modo.globalcomand", "glob");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender arg0, String[] arg1) {
		
		ProxiedPlayer p = (ProxiedPlayer) arg0;
		if (!p.hasPermission("modo.globalcomand")) {
			TextComponent c = new TextComponent("Vous n'avez pas la permission.");
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else if (arg1.length < 1) {
			TextComponent c = new TextComponent("Aucun message envoyé.");
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else {
			plugin.getPublisher().publish(new Publisher.PendingMessage("globmessages." + arg0.getName(), StringUtils.join(Arrays.copyOfRange(arg1, 0, arg1.length), " ")));
		}
	}

}
