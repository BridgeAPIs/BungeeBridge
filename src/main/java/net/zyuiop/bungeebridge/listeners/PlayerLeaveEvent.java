package net.zyuiop.bungeebridge.listeners;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.zyuiop.bungeebridge.BungeeBridge;

public class PlayerLeaveEvent implements Listener {

	protected BungeeBridge plugin;

	public PlayerLeaveEvent(BungeeBridge plugin) {
		this.plugin = plugin;
	}

	/**
	 * Déconnexion du joueur, on le supprime #MemoryLeaks
	 * @param event
	 */
	@EventHandler
	public void onPlayerLeave(final PlayerDisconnectEvent event) {
		plugin.getLobbySwitcher().affected_Lobbys.remove(event.getPlayer().getUniqueId());
		//plugin.partiesManager.logout(event.getPlayer().getUniqueId(), event.getPlayer().getName());
		plugin.getChatListener().removeMute(event.getPlayer().getUniqueId());
	}
}
