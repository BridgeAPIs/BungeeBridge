package net.zyuiop.bungeebridge.core.servers;

import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub {

	private final ServersManager bridge;

	public Subscriber(ServersManager bridge) {
		this.bridge = bridge;
	}

	@Override
	public void onMessage(String channel, String message) {
		String[] content = message.split(" ");
		if (content[0].equalsIgnoreCase("heartbeat")) {
			if (content.length < 3)
				return;

			String name = content[1];
			String ip = content[2];
			String port = content[3];
			bridge.heartBeet(name, ip, port);
		} else if (content[0].equalsIgnoreCase("stop")) {
			if (content.length < 1)
				return;

			String name = content[1];
			bridge.remove(name);
		}
	}
}
