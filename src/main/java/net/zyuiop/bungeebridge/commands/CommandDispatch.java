package net.zyuiop.bungeebridge.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CommandDispatch extends Command {
	public CommandDispatch() {
		super("dispatch", "proxies.dispatch");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if (strings.length < 3) {
			commandSender.sendMessage(new ComponentBuilder("Utilisation : /dispatch <proxies|servers> <all|servername> <command>").color(ChatColor.RED).create());
		} else {
			String selector = strings[0];
			String subselector = strings[1];
			String command = StringUtils.join(Arrays.copyOfRange(strings, 2, strings.length), " ");

			switch (selector) {
				case "proxies":
					BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("commands.proxies." + subselector, command));
					commandSender.sendMessage(new ComponentBuilder("La commande a été émise.").color(ChatColor.GREEN).create());
					break;
				case "servers":
					BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("commands.servers." + subselector, command));
					commandSender.sendMessage(new ComponentBuilder("La commande a été émise.").color(ChatColor.GREEN).create());
					break;
				default:
					commandSender.sendMessage(new ComponentBuilder("Le selecteur donné n'est pas valable.").color(ChatColor.RED).create());
					break;
			}
		}
	}
}
