package net.bridgesapis.bungeebridge.core.proxies;

import net.bridgesapis.bungeebridge.core.handlers.SubscribingThread;
import net.md_5.bungee.api.ProxyServer;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.database.Publisher;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NetworkBridge {

	private final BungeeBridge plugin;
	private final String proxy;
	private final SubscribingThread thread;
	private ConcurrentHashMap<String, RemoteProxy> proxies = new ConcurrentHashMap<>();

	public NetworkBridge(BungeeBridge plugin) {
		this.plugin = plugin;
		this.proxy = plugin.getProxyName();

		plugin.getLogger().info("Initialized NetworkBridge for proxy " + proxy);

		// Recover proxys //
		Jedis jedis = plugin.getConnector().getBungeeResource();
		Set<String> proxys = jedis.smembers("proxys");
		jedis.close();
		for (String proxy : proxys)
			if (!proxy.equals(this.proxy))
				proxies.put(proxy, new RemoteProxy(null, proxy));


		// Init task
		ProxyServer.getInstance().getScheduler().schedule(plugin, this::heartBeet, 15, 15, TimeUnit.SECONDS);

		thread = new SubscribingThread(SubscribingThread.Type.SUBSCRIBE, plugin.getConnector(), new Subscriber(this), "proxybridge");
		new Thread(thread).start();

		ProxyServer.getInstance().getPluginManager().registerListener(plugin, new BridgeListener(this));
	}

	void heartBeet() {
		plugin.getPublisher().publish(new Publisher.PendingMessage("proxybridge", "heartbeat " + proxy));

		proxies.values().stream().filter(proxy -> ! proxy.isOnline()).forEach(proxy -> {
			ProxyServer.getInstance().getLogger().severe("[HeartBeet] Proxy " + proxy.getName() + " detected as offline, will be removed.");
			proxies.remove(proxy.getName());
		});
	}

	public void heartBeet(String proxy) {
		if (this.proxy.equals(proxy))
			return;

		RemoteProxy proxy1 = proxies.get(proxy);
		if (proxy1 == null)
			proxies.put(proxy, new RemoteProxy(new Date(), proxy));
		else
			proxy1.heartBeet();
	}

	public void disable() {
		plugin.getLogger().info("[Disabling Bridge] Clearing old cache...");
		Jedis jedis = plugin.getConnector().getBungeeResource();
		jedis.srem("proxys", proxy);
		jedis.del("connected:" + proxy);
		jedis.close();


		thread.disable();
	}

	public Set<String> getProxies() {
		Set<String> ret = new HashSet<>();
		ret.addAll(proxies.keySet());
		ret.add(proxy);
		return ret;
	}

	protected BungeeBridge getPlugin() {
		return plugin;
	}

	public Set<UUID> getPlayersOnline() {
		Set<UUID> ret = new HashSet<>();
		proxies.values().stream().filter(RemoteProxy::isOnline).forEach(proxy -> ret.addAll(proxy.getPlayers()));
		ProxyServer.getInstance().getPlayers().stream().forEach(player -> ret.add(player.getUniqueId()));
		return ret;
	}

	public boolean isOnline(UUID player) {
		return getPlayersOnline().contains(player);
	}

	public Set<String> getOnlinePlayersNames() {
		Set<String> ret = new HashSet<>();
		for (UUID id : getPlayersOnline()) {
			String name = plugin.getUuidTranslator().getName(id, false);
			if (name != null)
				ret.add(name);
		}
		return ret;
	}

	protected static class RemoteProxy {
		private Date lastHeartbeet;
		private String name;

		public RemoteProxy(Date lastHeartbeet, String name) {
			this.lastHeartbeet = lastHeartbeet;
			this.name = name;
		}

		public void heartBeet() {
			lastHeartbeet = new Date();
		}

		public boolean isOnline() {
			Date date = new Date(System.currentTimeMillis() - 30000);
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

		public void setName(String name) {
			this.name = name;
		}

		public Set<UUID> getPlayers() {
			Jedis jedis = BungeeBridge.getInstance().getConnector().getBungeeResource();
			Set<String> list = jedis.smembers("connected:" + name);
			jedis.close();

			Set<UUID> ret = new HashSet<>();
			for (String uid : list)
				ret.add(UUID.fromString(uid));

			return ret;
		}
	}
}
