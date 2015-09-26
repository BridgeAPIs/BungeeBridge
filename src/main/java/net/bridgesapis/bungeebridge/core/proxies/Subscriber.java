package net.bridgesapis.bungeebridge.core.proxies;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(id);
			if (player != null) {
				ServerInfo info = ProxyServer.getInstance().getServerInfo(to);
				if (info != null) {
					player.connect(info);
				}
			}
		} else if (content[0].equalsIgnoreCase("heartbeat")) {
			if (content.length < 2)
				return;

			String proxy = content[1];
			bridge.heartBeet(proxy);
		}
	}

}
