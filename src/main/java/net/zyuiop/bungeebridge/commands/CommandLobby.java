package net.zyuiop.bungeebridge.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.lobbys.LobbyServer;

public class CommandLobby extends Command {

    private final BungeeBridge plugin;

	public CommandLobby(BungeeBridge plugin) {
		super("lobby", null, "hub");
        this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender arg0, String[] arg1) {
		ProxiedPlayer p = (ProxiedPlayer) arg0;

        if (arg1.length > 0) {
            try {
                Integer lnumber = Integer.decode(arg1[0]);
                LobbyServer s = plugin.getLobbySwitcher().lobbyData.getServerByName("Lobby_"+lnumber);
                if (s != null) {
                    if (!s.isOnline()) {
                        arg0.sendMessage(ChatColor.RED+"Ce lobby est hors ligne.");
                    } else if (s.getPlayerCount() > s.getMaxPlayers()) {
                        arg0.sendMessage(ChatColor.RED+"Ce lobby est plein.");
                    } else {
                        ServerInfo i = ProxyServer.getInstance().getServerInfo(s.getServerName());
                        p.connect(i);
                    }
                    return;
                }
            } catch (NumberFormatException ignored) {
                arg0.sendMessage(ChatColor.RED + "Numéro de lobby invalide.");
                return;
            }
        }

		p.connect(ProxyServer.getInstance().getServerInfo("lobby"));
	}

}
