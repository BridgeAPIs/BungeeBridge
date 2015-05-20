package net.zyuiop.bungeebridge.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.utils.SettingsManager;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;
import redis.clients.jedis.Jedis;

public class ServerMovementsListener implements Listener {

    protected BungeeBridge plugin;

    public ServerMovementsListener(BungeeBridge plugin) {
        this.plugin = plugin;
    }

    /**
     * Lors du kick du joueur d'un serveur
     * Permet d'éviter que le joueur soit kick du proxy
     * @param event
     */
	@EventHandler
	public void onPlayerKick(ServerKickEvent event) {
		event.getPlayer().sendMessage(event.getKickReasonComponent());
		event.setCancelled(true);
		ServerInfo server = event.getKickedFrom();
		if(server.getName().startsWith("Lobby_")) {
			return;
		}

		if (event.getState() == ServerKickEvent.State.CONNECTING) {
			ProxyServer.getInstance().getLogger().info("[Server '" + event.getKickedFrom().getName() + "' refused connection]  "+event.getPlayer().getDisplayName() + ", reason " + event.getKickReasonComponent()[0].toPlainText());
			server = event.getPlayer().getServer().getInfo();
			event.setCancelServer(server);
		}
	}


    /**
     * Lorsque le joueur demande à se connecter à un serveur
     * @param e
     */
	@EventHandler (priority = EventPriority.LOW)
	public void onConnect(final ServerConnectEvent e) {
		final ProxiedPlayer p = e.getPlayer();

		if (p.getServer() != null && p.getServer().getInfo().equals(e.getTarget())) {
			// On évite les You're already connected on this server

			return;
		}

		if(e.getTarget().getName().equals("main")) {
			ProxyServer.getInstance().getLogger().info("[Server kicked player] " + e.getPlayer().getDisplayName());

			Server old = e.getPlayer().getServer();
			if (old != null) {
				plugin.getLobbySwitcher().kickFromLobby(e.getPlayer(), old.getInfo().getName());
			} else {
				plugin.getLobbySwitcher().cursiveJoin(e.getPlayer());
			}

			confirmConnection(e);
		}

		if(!e.getTarget().getName().equals("lobby")) {
			return;
		}

		confirmConnection(e);

	}

	public void confirmConnection(final ServerConnectEvent e) {
		ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(plugin.getLobbySwitcher().cursiveJoin(e.getPlayer()));
		if(serverInfo != null) {
			e.setTarget(serverInfo);
		} else {
			plugin.getLobbySwitcher().connectMain(e.getPlayer());
			e.setCancelled(true);
		}
	}

	/**
	 * Le joueur finit sa connexion au serveur. On sauvegarde son lobby
	 * @param e L'évènement de fin de connexion
	 */
	@EventHandler
	public void onFinishConnect(final ServerConnectedEvent e) {
		plugin.getExecutor().addTask( () -> {
			PermissionUser user = plugin.getPermissionsBridge().getApi().getUser(e.getPlayer().getUniqueId());
			if (user.hasPermission("tracker.famous")) {
				String display = user.getProperty("display");
				if (display == null)
					display = ChatColor.GRAY + "[Joueur]";
				display = display.replaceAll("&s", " ");
				display = ChatColor.translateAlternateColorCodes('&', display);
				String server = e.getServer().getInfo().getName();
				if (! SettingsManager.isEnabled(e.getPlayer().getUniqueId(), "tracking", true))
					server = "hidden";

				Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
				jedis.hset("famouslocations", e.getPlayer().getUniqueId().toString(), display + e.getPlayer().getName() + "::" + server);
				jedis.close();
			}});

	}
}
