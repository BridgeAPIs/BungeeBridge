package net.bridgesapis.bungeebridge.listeners;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PlayerJoinEvent implements Listener {

	protected BungeeBridge plugin;
	private static Set<Callback<ProxiedPlayer>> triggerOnJoin = new HashSet<>();

	public PlayerJoinEvent(BungeeBridge plugin) {
		this.plugin = plugin;
	}

	public static void triggerOnJoin(Callback<ProxiedPlayer> trigger) {
		triggerOnJoin.add(trigger);
	}

	@EventHandler
	public void onJoin(final PostLoginEvent e) {
		final ProxiedPlayer p = e.getPlayer();
		ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
			TextComponent welcome = new TextComponent("Bienvenue, " + p.getName() + " !");
			welcome.setColor(ChatColor.GOLD);


			String key = "rejoinlist:" + e.getPlayer().getUniqueId().toString();
			Jedis cache = plugin.getConnector().getBungeeResource();
			String srv = cache.get(key);
			cache.close();
			if(srv != null) {
				final ServerInfo server = ProxyServer.getInstance().getServerInfo(srv);
				if(server != null)
					ProxyServer.getInstance().getScheduler().schedule(plugin, () -> e.getPlayer().connect(server, (aBoolean, throwable) -> {
						if (aBoolean) {
							p.sendMessage(new ComponentBuilder("").color(ChatColor.GREEN).append("Vous avez été remis en jeu.").create());
						}
					}), 200L, TimeUnit.MILLISECONDS);
			}
		});
	}
}
