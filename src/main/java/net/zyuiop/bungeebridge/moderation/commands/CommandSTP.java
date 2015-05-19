package net.zyuiop.bungeebridge.moderation.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;

public class CommandSTP extends ModCommand {
    public CommandSTP(BungeeBridge pl) {
        super("stp", "modoutils.tp", pl);
    }

    @Override
    public void execute(final CommandSender arg0, final String[] arg1) {
            ProxiedPlayer p = (ProxiedPlayer) arg0;

			// Vérification des arguments
			if (arg1.length < 1) {
				TextComponent message = new TextComponent("Syntaxe : /stp <serveur>");
				message.setColor(ChatColor.RED);
				arg0.sendMessage(message);
				return;
			}

			String server = arg1[0];
			ServerInfo info = ProxyServer.getInstance().getServerInfo(server);
			if (info == null) {
				TextComponent message = new TextComponent("Le serveur recherché n'existe pas.");
				message.setColor(ChatColor.RED);
				arg0.sendMessage(message);
			} else {
				bridge.getPublisher().publish(new Publisher.PendingMessage(server, "moderator " + p.getUniqueId(), () -> p.connect(info)));
            }
    }
}
