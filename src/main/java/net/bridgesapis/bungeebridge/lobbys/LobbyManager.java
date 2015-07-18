package net.bridgesapis.bungeebridge.lobbys;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zyuiop on 26/05/15.
 * Licensed under GNU LGPL license
 */
public class LobbyManager {

	private final Cache<UUID, String> repartition = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.HOURS).build();
	private final BungeeBridge plugin;
	private final Random random;
	private final ConcurrentHashMap<String, ServerInfo> lobbies = new ConcurrentHashMap<>();
	private final String lobbyPrefix;

	public LobbyManager(BungeeBridge plugin, String lobbyPrefix) {
		this.plugin = plugin;
		this.random = new Random();
		this.lobbyPrefix = lobbyPrefix;
	}

	public String getLobbyPrefix() {
		return lobbyPrefix;
	}

	private void recoverHubs() {
		ProxyServer.getInstance().getServers().values().stream().filter(info -> ! lobbies.containsKey(info.getName())).forEach(info -> lobbies.put(info.getName(), info));
		lobbies.keySet().stream().filter(key -> ! ProxyServer.getInstance().getServers().containsKey(key)).forEach(lobbies::remove);
	}

	public ServerInfo joinHub(ProxiedPlayer player) {
		String name = repartition.getIfPresent(player.getUniqueId());
		if (name != null && !lobbies.containsKey(name))
			name = null;

		if (name == null) {
			String[] keys = (String[]) repartition.asMap().keySet().toArray();
			name = keys[random.nextInt(keys.length)];
		}

		return lobbies.get(name);
	}

	public ServerInfo getByName(String name) {
		return lobbies.get(name);
	}

	public ServerInfo getByNumber(Integer number) {
		return lobbies.get(getLobbyPrefix() + "_" + number);
	}

	public void save(UUID player, String hub) {
		repartition.put(player, hub);
	}
}
