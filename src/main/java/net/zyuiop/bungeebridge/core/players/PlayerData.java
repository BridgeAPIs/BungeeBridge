package net.zyuiop.bungeebridge.core.players;

import net.zyuiop.bungeebridge.BungeeBridge;
import redis.clients.jedis.Jedis;

import java.util.*;

public class PlayerData extends PlayerDataAbstract {

	private final BungeeBridge plugin;
	private Date lastRefresh = null;

	public PlayerData(UUID player, BungeeBridge bridge) {
		super(player);
		this.plugin = bridge;
		updateData();
	}

	protected void updateData() {
		Jedis jedis = plugin.getConnector().getResource();
		Map<String, String> data = jedis.hgetAll("player:" + playerID);
		jedis.close();
		this.playerData = data;
		this.lastRefresh = new Date();
	}

	protected void refreshIfNeeded() {
		if (lastRefresh == null || (lastRefresh.getTime() + (1000 * 60 * 5)) < System.currentTimeMillis()) {
			updateData();
		}
	}

	@Override
	public String get(String key) {
		refreshIfNeeded();
		return super.get(key);
	}

	@Override
	public Set<String> getKeys() {
		refreshIfNeeded();
		return super.getKeys();
	}

	@Override
	public Map<String, String> getValues() {
		refreshIfNeeded();
		return super.getValues();
	}

	@Override
	public boolean contains(String key) {
		refreshIfNeeded();
		return super.contains(key);
	}

	@Override
	public void set(String key, String value) {
		this.playerData.put(key, value);

		plugin.getExecutor().addTask(() -> {
			Jedis jedis = plugin.getConnector().getResource();
			jedis.hset("player:" + playerID, key, value);
			jedis.close();
		});
	}

	@Override
	public void remove(String key) {
		playerData.remove(key);
		plugin.getExecutor().addTask(() -> {
			Jedis jedis = plugin.getConnector().getResource();
			jedis.hdel("player:" + playerID, key);
			jedis.close();
		});
	}

	@Override
	public void setInt(String key, int value) {
		set(key, String.valueOf(value));
	}

	@Override
	public void setBoolean(String key, boolean value) {
		set(key, String.valueOf(value));
	}

	@Override
	public void setDouble(String key, double value) {
		set(key, String.valueOf(value));
	}

	@Override
	public void setLong(String key, long value) {
		set(key, String.valueOf(value));
	}


}
