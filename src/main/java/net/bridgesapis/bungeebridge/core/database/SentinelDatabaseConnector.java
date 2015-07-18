package net.bridgesapis.bungeebridge.core.database;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.handlers.MainSubscriber;
import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;
import net.bridgesapis.bungeebridge.core.handlers.SubscribingThread;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Set;

public class SentinelDatabaseConnector extends DatabaseConnector {

	protected JedisSentinelPool mainPool;
	protected JedisSentinelPool cachePool;
	protected Set<String> sentinels;
	protected String mainMonitorName;
	protected String cacheMonitorName;

	public SentinelDatabaseConnector(BungeeBridge plugin, Set<String> sentinels, String mainMonitor, String cacheMonitor, String password) {
		this.plugin = plugin;
		this.sentinels = sentinels;
		this.mainMonitorName = mainMonitor;
		this.cacheMonitorName = cacheMonitor;
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

		if (password == null || password.length() == 0) {
			this.mainPool = new JedisSentinelPool(mainMonitorName, sentinels, config);
			this.cachePool = new JedisSentinelPool(cacheMonitorName, sentinels, config);
		} else {
			this.mainPool = new JedisSentinelPool(mainMonitorName, sentinels, config, password);
			this.cachePool = new JedisSentinelPool(cacheMonitorName, sentinels, config, password);
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
