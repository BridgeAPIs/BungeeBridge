package net.zyuiop.bungeebridge.core.handlers;

import net.md_5.bungee.api.ProxyServer;
import net.zyuiop.bungeebridge.core.database.DatabaseConnector;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class SubscribingThread implements Runnable {

	private final Type type;
	private final String[] channels;
	private final JedisPubSub pubSub;
	private final DatabaseConnector connector;
	private boolean enabled = true;

	public SubscribingThread(Type type, DatabaseConnector connector, JedisPubSub pubSub, String... channels) {
		this.type = type;
		this.connector = connector;
		this.channels = channels;
		this.pubSub = pubSub;
	}

	@Override
	public void run() {
		while (enabled) {
			try {
				Jedis jedis = connector.getResource();
				ProxyServer.getInstance().getLogger().info("[DB-WatchDog] Starting subscribing with type " + ((type == Type.SUBSCRIBE) ? "SUBSCRIBE" : "PSUBSCRIBE") + ". Channels to subscribe on : " + StringUtils.join(channels, "; "));

				try {
					if (type == Type.SUBSCRIBE)
						jedis.subscribe(pubSub, channels);
					else
						jedis.psubscribe(pubSub, channels);
				} catch (Exception e) {
					ProxyServer.getInstance().getLogger().severe("[DB-WatchDog] Exception during subscription : " + e.getMessage());
				}

				ProxyServer.getInstance().getLogger().info("[DB-WatchDog] Closing subscription. " + (((! enabled) ? "Stopping thread." : "Trying to restart.")));
				jedis.close();
			} catch (Exception e) {
				ProxyServer.getInstance().getLogger().severe("[DB-WATCHDOG] Exception while getting database : " + e.getMessage() + ". Retrying in 5 seconds.");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void disable() {
		enabled = false;
		pubSub.punsubscribe();
		pubSub.unsubscribe();
	}

	public enum Type {
		SUBSCRIBE, PSUBSCRIBE
	}
}
