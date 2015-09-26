package net.bridgesapis.bungeebridge.core.handlers;

import com.google.gson.Gson;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class ApiExecutor implements PubSubConsumer {

	/*
	Protocol documentation :
	apiexec.send <nickname> <json encoded message>
	apiexec.kick <nickname> <json encoded reason>
	apiexec.connect <nickname> <server>
	apiexec.friendrequest <from> <to>
	 */

	private static final HashMap<String, CustomExecutor> executorHashMap = new HashMap<>();

	@Override
	public void consume(final String channel, String message) {
		final String[] chan = StringUtils.split(channel, ".");

		String[] msgParts = message.split(" ");
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(msgParts[0]));
		if (player == null)
			return;


		if (chan[1].equalsIgnoreCase("send")) {
			TextComponent data = new Gson().fromJson(StringUtils.join(Arrays.copyOfRange(msgParts, 1, msgParts.length), " "), TextComponent.class);
			player.sendMessage(data);
		} else if (chan[1].equalsIgnoreCase("kick")) {
			TextComponent data = new Gson().fromJson(StringUtils.join(Arrays.copyOfRange(msgParts, 1, msgParts.length), " "), TextComponent.class);
			player.disconnect(data);
		} else if (chan[1].equalsIgnoreCase("connect")) {
			String server = msgParts[0];
			ServerInfo info = ProxyServer.getInstance().getServerInfo(server);
			if (info != null)
				player.connect(info);
		} else {
			CustomExecutor executor = executorHashMap.get(chan[1]);
			if (executor != null)
				executor.consume(msgParts);
		}
	}

	public static void registerExecutor(String channel, CustomExecutor executor) {
		executorHashMap.put(channel, executor);
	}

	public interface CustomExecutor {
		void consume(String[] message);
	}
}
