package net.bridgesapis.bungeebridge.interactions.privatemessages;

import net.bridgesapis.bungeebridge.commands.DefaultExecutor;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.bridgesapis.bungeebridge.BungeeBridge;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CommandMsg extends DefaultExecutor {

	private BungeeBridge plugin;

	public CommandMsg(BungeeBridge plugin) {
		super("msg", null, "w");
		this.plugin = plugin;
	}

	@Override
	public void execute(final CommandSender arg0, final String[] arg1) {
		if (arg1.length < 2) {
			TextComponent msg = new TextComponent(I18n.getModuleTranslation("messages", "commands.msg.missing_argument"));
			msg.setColor(ChatColor.RED);
			arg0.sendMessage(msg);
			return;
		}
		
		if (!(arg0 instanceof ProxiedPlayer)) {
			TextComponent msg = new TextComponent(I18n.getTranslation("commands.misc.not_a_player"));
            msg.setColor(ChatColor.RED);
            arg0.sendMessage(msg);
            return;
		}
		
		
		ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> plugin.getPrivateMessagesManager().send((ProxiedPlayer) arg0, arg1[0], StringUtils.join(Arrays.copyOfRange(arg1, 1, arg1.length), " ")));
	}
}
