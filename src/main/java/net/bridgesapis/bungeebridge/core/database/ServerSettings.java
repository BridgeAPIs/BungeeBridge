package net.bridgesapis.bungeebridge.core.database;

import net.bridgesapis.bungeebridge.BungeeBridge;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class ServerSettings {

	private CloseType type;
	private String serverLine;
	private String motd;
	private int maxPlayers;
	private boolean allowJoin = false;

	public void refresh() {
		Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
		Map<String, String> values = jedis.hgetAll("serversettings");
		jedis.close();

		if (values == null)
			return;

		if (values.containsKey("closetype"))
			type = CloseType.fromString(values.get("closetype"));

		if (values.containsKey("serverLine"))
			serverLine = values.get("serverLine");

		if (values.containsKey("motd"))
			motd = values.get("motd");

		if (values.containsKey("maxplayers"))
			maxPlayers = Integer.valueOf(values.get("maxplayers"));
	}

	public boolean isAllowJoin() {
		return allowJoin;
	}

	public void setAllowJoin(boolean allowJoin) {
		this.allowJoin = allowJoin;
	}

	public void updateValue(String key, String value) {
		BungeeBridge.getInstance().getExecutor().addTask(() -> {
			Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
			jedis.hset("serversettings", key, value);
			jedis.close();
		});
	}

	public CloseType getType() {
		return type;
	}

	public void setType(CloseType type) {
		this.type = type;
		updateValue("closetype", type.getString());
	}

	public String getServerLine() {
		return serverLine;
	}

	public void setServerLine(String serverLine) {
		this.serverLine = serverLine;
		updateValue("serverLine", serverLine);
	}

	public String getMotd() {
		return motd;
	}

	public void setMotd(String motd) {
		this.motd = motd;
		updateValue("motd", motd);
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
		updateValue("maxplayers", String.valueOf(maxPlayers));
	}

	public enum CloseType {
		VIP("vip"), CLOSED("closed"), OPENED("opened");

		private String string;
		private CloseType(String string) {
			this.string = string;
		}

		public static CloseType fromString(String str) {
			for (CloseType type : CloseType.values())
				if (str.equalsIgnoreCase(type.getString()))
					return type;
			return CLOSED;
		}

		public String getString() {
			return string;
		}
	}

}
