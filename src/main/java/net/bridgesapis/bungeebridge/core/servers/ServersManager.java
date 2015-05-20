package net.bridgesapis.bungeebridge.core.servers;

import net.bridgesapis.bungeebridge.core.handlers.SubscribingThread;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.players.PlayerData;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServersManager {

	private final BungeeBridge plugin;
	private ConcurrentHashMap<String, RemoteServer> servers = new ConcurrentHashMap<>();
	private SubscribingThread thread;

	public ServersManager(BungeeBridge plugin) {
		this.plugin = plugin;

		// Recover servers //
		Jedis jedis = plugin.getConnector().getBungeeResource();
		Map<String, String> servers = jedis.hgetAll("servers");
		jedis.close();
		for (String server : servers.keySet()) {
			String[] ip = servers.get(server).split(":");
			if (ip.length == 2)
				createServer(server, ip[0], ip[1]);
		}


		// Init task
		ProxyServer.getInstance().getScheduler().schedule(plugin, this::checkServers, 30, 30, TimeUnit.SECONDS);

		thread = new SubscribingThread(SubscribingThread.Type.SUBSCRIBE, plugin.getConnector(), new Subscriber(this), "servers");
		new Thread(thread).start();
	}

	public void createServer(String server, String ip, String port) {
		InetSocketAddress address = new InetSocketAddress(ip, Integer.parseInt(port));

		ServerInfo info = ProxyServer.getInstance().constructServerInfo(server, address, "Automatically added server", false);
		ProxyServer.getInstance().getServers().put(server, info);

		RemoteServer remoteServer = new RemoteServer(new Date(), server, info);
		this.servers.put(server, remoteServer);

		ProxyServer.getInstance().getLogger().info("[Servers] Created server " + server + ", " + ip + ":" + port);

		BungeeBridge.getInstance().getExecutor().addTask(() -> {
			Jedis jedis = BungeeBridge.getInstance().getConnector().getBungeeResource();
			jedis.srem("offlineservers", server);
			jedis.close();
		});
	}

	void checkServers() {
		servers.values().stream().filter(server -> ! server.isOnline()).forEach(server -> {
			ProxyServer.getInstance().getLogger().severe("[Servers] Server " + server.getName() + " detected as offline, removing.");
			remove(server.getName());
			BungeeBridge.getInstance().getExecutor().addTask(() -> {
				Jedis jedis = BungeeBridge.getInstance().getConnector().getBungeeResource();
				jedis.hdel("servers", server.getName());
				jedis.sadd("offlineservers", server.getName());
				jedis.del("connectedonserv:" + server.getName());
				jedis.publish("servers", "stop " + server.getName());
				jedis.close();
			});
		});
	}

	public void heartBeet(String name, String ip, String port) {
		RemoteServer server = servers.get(name);
		if (server == null)
			createServer(name, ip, port);
		else
			server.heartBeet();
	}

	public void disable() {
		plugin.getLogger().info("[Disabling Servers Manager] Killing subscribtions...");
		thread.disable();
	}

	protected BungeeBridge getPlugin() {
		return plugin;
	}

	public void remove(String name) {
		servers.remove(name);
		ProxyServer.getInstance().getServers().remove(name);
	}

	public Set<String> getServers() {
		return servers.keySet();
	}

	public ServersManager.RemoteServer getServer(String server) {
		return servers.get(server);
	}

	public ServerInfo getServer(UUID player) {
		PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
		String server = data.get("currentserver");
		if (server == null)
			return null;
		return ProxyServer.getInstance().getServerInfo(server);
	}

	public static class RemoteServer {
		private Date lastHeartbeet;
		private String name;
		private ServerInfo info;

		public RemoteServer(Date lastHeartbeet, String name, ServerInfo info) {
			this.lastHeartbeet = lastHeartbeet;
			this.name = name;
			this.info = info;
		}

		public ServerInfo getInfo() {
			return info;
		}

		public void setInfo(ServerInfo info) {
			this.info = info;
		}

		public void heartBeet() {
			lastHeartbeet = new Date();
		}

		public boolean isOnline() {
			Date date = new Date(System.currentTimeMillis() - 120000);
			return !(lastHeartbeet == null || lastHeartbeet.before(date));
		}

		public Date getLastHeartbeet() {
			return lastHeartbeet;
		}

		public void setLastHeartbeet(Date lastHeartbeet) {
			this.lastHeartbeet = lastHeartbeet;
		}

		public String getName() {
			return name;
		}

		public Set<UUID> getPlayers() {
			Jedis jedis = BungeeBridge.getInstance().getConnector().getBungeeResource();
			Set<String> list = jedis.smembers("connectedonserv:" + name);
			jedis.close();

			return list.stream().map(UUID::fromString).collect(Collectors.toSet());
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
