package net.bridgesapis.bungeebridge.listeners;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerMovementsListener implements Listener {

    protected BungeeBridge plugin;

    public ServerMovementsListener(BungeeBridge plugin) {
        this.plugin = plugin;
    }

	/**
	 * Avoid the player to be kicked from the proxy when kicked from a game
	 * @param event The ServerKickEvent
	 */
	@EventHandler
	public void onPlayerKick(ServerKickEvent event) {
		event.getPlayer().sendMessage(event.getKickReasonComponent());
		event.setCancelled(true);

		if (event.getState() == ServerKickEvent.State.CONNECTING) {
			ProxyServer.getInstance().getLogger().info("[Server '" + event.getKickedFrom().getName() + "' refused connection]  "+event.getPlayer().getDisplayName() + ", reason " + event.getKickReasonComponent()[0].toPlainText());
			ServerInfo server = event.getPlayer().getServer().getInfo();
			event.setCancelServer(server);
		}
	}
}
