package net.bridgesapis.bungeebridge.moderation.commands;

import net.bridgesapis.bungeebridge.moderation.JsonModMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.bridgesapis.bungeebridge.BungeeBridge;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CommandMod extends ModCommand {

	public CommandMod(BungeeBridge plugin) {
		super("mod", "modo.modchan", plugin);
	}

	@Override
	public void execute(final CommandSender p, String[] arg1) {
		if (!p.hasPermission("modo.modchan")) {
			TextComponent c = new TextComponent("Vous n'avez pas la permission.");
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else if (arg1.length < 1) {
			TextComponent c = new TextComponent("Aucun message envoyé.");
			c.setColor(ChatColor.RED);
			p.sendMessage(c);
		} else {
            final String motif = StringUtils.join(Arrays.copyOfRange(arg1, 0, arg1.length), " ");
			JsonModMessage.build(p, motif).send();
		}
	}

}
