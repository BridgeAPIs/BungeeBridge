package net.bridgesapis.bungeebridge.core.database;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.handlers.MainSubscriber;
import net.bridgesapis.bungeebridge.core.handlers.SubscribingThread;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;

import java.net.URI;
import java.util.Set;

public class SingleDatabaseConnector extends DatabaseConnector {

	private final String cacheIp;
	private final String masterIp;
	protected JedisPool mainPool;
	protected JedisPool cachePool;

	public SingleDatabaseConnector(BungeeBridge plugin, String masterIp, String cacheIp, String password) {
		this.plugin = plugin;
		this.masterIp = masterIp;
		this.cacheIp = cacheIp;
		this.password = password;

		plugin.getLogger().info("[Database] Initializing connection.");
		try {
			initiateConnections();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Jedis getResource() {
		return mainPool.getResource();
	}

	@Override
	public Jedis getBungeeResource() {
		return cachePool.getResource();
	}

	@Override
	public void killConnections() {
		cachePool.destroy();
		mainPool.destroy();
	}

	@Override
	public void initiateConnections() throws InterruptedException {
		// Préparation de la connexion
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(1024);
		config.setMaxWaitMillis(5000);

		String[] mainParts = StringUtils.split(masterIp, ":");
		int mainPort = (mainParts.length > 1) ? Integer.decode(mainParts[1]) : 6379;

		String[] cacheParts = StringUtils.split(cacheIp, ":");
		int cachePort = (cacheParts.length > 1) ? Integer.decode(cacheParts[1]) : 6379;

		if (password == null || password.length() == 0) {
			this.mainPool = new JedisPool(config, mainParts[0], mainPort, 5000);
			this.cachePool = new JedisPool(config, cacheParts[0], cachePort, 5000);
		} else {
			this.mainPool = new JedisPool(config, mainParts[0], mainPort, 5000, password);
			this.cachePool = new JedisPool(config, cacheParts[0], cachePort, 5000, password);
		}

		plugin.getLogger().info("[Database] Connection initialized.");

		this.commandsSubscriber = new MainSubscriber();

		thread = new SubscribingThread(SubscribingThread.Type.PSUBSCRIBE, this, commandsSubscriber, "*");
		new Thread(thread).start();

		while (!commandsSubscriber.isSubscribed())
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		while (!commandsSubscriber.isSubscribed())
			Thread.sleep(100);
	}

	@Override
	public MainSubscriber getCommandsSubscriber() {
		return commandsSubscriber;
	}

	@Override
	public void disable() {
		plugin.getLogger().info("[Disabling Connector] Killing subscriptions...");
		commandsSubscriber.unsubscribe();
		commandsSubscriber.punsubscribe();
		plugin.getLogger().info("[Disabling Connector] Closing subscriber connection...");
		thread.disable();
		plugin.getLogger().info("[Disabling Connector] Removing pools...");
		killConnections();
	}

}
