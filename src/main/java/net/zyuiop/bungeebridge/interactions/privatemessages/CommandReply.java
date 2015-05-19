package net.zyuiop.bungeebridge.interactions.privatemessages;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.commands.DefaultExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CommandReply extends DefaultExecutor {

	private BungeeBridge plugin;

	public CommandReply(BungeeBridge plugin) {
		super("r", null, "reply", "rep");
		this.plugin = plugin;
	}


	@Override
	public void execute(final CommandSender arg0, final String[] arg1) {
		if (arg1.length < 1) {
			TextComponent msg = new TextComponent("Aucun message précisé.");
			msg.setColor(ChatColor.RED);
			arg0.sendMessage(msg);
			return;
		}

		if (!(arg0 instanceof ProxiedPlayer)) {
			TextComponent msg = new TextComponent("La console ne peut pas envoyer de MPs.");
            msg.setColor(ChatColor.RED);
            arg0.sendMessage(msg);
            return;
		}

		String message = StringUtils.join(Arrays.copyOfRange(arg1, 0, arg1.length), " ");
		plugin.getPrivateMessagesManager().reply((ProxiedPlayer) arg0, message);
	}
}
