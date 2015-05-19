package net.zyuiop.bungeebridge.core.proxies;

import net.zyuiop.bungeebridge.BungeeBridge;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class Subscriber extends JedisPubSub {

	private final NetworkBridge bridge;

	public Subscriber(NetworkBridge bridge) {
		this.bridge = bridge;
	}

	@Override
	public void onMessage(String channel, String message) {
		String[] content = message.split(" ");
		if (content[0].equalsIgnoreCase("login")) {
			if (content.length < 3)
				return;

			UUID id = UUID.fromString(content[1]);
			String name = content[2];

			BungeeBridge.getInstance().getExecutor().addTask(() -> {
				try {
					BungeeBridge.getInstance().getPartiesManager().comeBack(id);
				} catch (Exception ignored) {	}
			});
		} else if (content[0].equalsIgnoreCase("logout")) {
			if (content.length < 3)
				return;

			UUID id = UUID.fromString(content[1]);
			String name = content[2];
		} else if (content[0].equalsIgnoreCase("move")) {
			if (content.length < 3)
				return;

			UUID id = UUID.fromString(content[1]);
			String to = content[2];
		} else if (content[0].equalsIgnoreCase("heartbeat")) {
			if (content.length < 2)
				return;

			String proxy = content[1];
			bridge.heartBeet(proxy);
		}
	}

}
