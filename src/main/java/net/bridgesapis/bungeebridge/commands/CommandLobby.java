package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.bridgesapis.bungeebridge.BungeeBridge;

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
				ServerInfo info = plugin.getLobbySwitcher().getByNumber(lnumber);
                if (info != null) {
                    p.connect(info);
					return;
                }
            } catch (NumberFormatException ignored) {
                arg0.sendMessage(ChatColor.RED + I18n.getTranslation("commands.lobby.invalid"));
                return;
            }
        }

		p.connect(ProxyServer.getInstance().getServerInfo("lobby"));
	}

}
