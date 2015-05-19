package net.zyuiop.bungeebridge.core.players;

import net.zyuiop.bungeebridge.BungeeBridge;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class PlayerDataManager {

	protected ConcurrentHashMap<UUID, PlayerData> cachedData = new ConcurrentHashMap<>();
	private final BungeeBridge bungeeBridge;

	public PlayerDataManager(BungeeBridge bungeeBridge) {
		this.bungeeBridge = bungeeBridge;
	}


	public PlayerData getPlayerData(UUID player) {
		return getPlayerData(player, false);
	}

	public PlayerData getPlayerData(UUID player, boolean forceRefresh) {
		if (!cachedData.containsKey(player)) {
			PlayerData data = new PlayerData(player, bungeeBridge);
			cachedData.put(player, data);
			return data;
		}

		PlayerData data = cachedData.get(player);

		if (forceRefresh) {
			data.updateData();
			return data;
		}

		data.refreshIfNeeded();
		return data;
	}

	public void update(UUID player) {
		if (!cachedData.containsKey(player)) {
			PlayerData data = new PlayerData(player, bungeeBridge);
			cachedData.put(player, data);
			return;
		}

		PlayerData data = cachedData.get(player);
		data.updateData();
	}

	public void unload(UUID player) {
		cachedData.remove(player);
	}
}
