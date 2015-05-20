package net.zyuiop.bungeebridge.core.handlers;

import com.google.gson.Gson;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.UUID;

public class ApiExecutor implements PubSubConsumer {

	/*
	Protocol documentation :
	apiexec.send <nickname> <json encoded message>
	apiexec.kick <nickname> <json encoded reason>
	apiexec.connect <nickname> <server>
	apiexec.friendrequest <from> <to>
	 */

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
		} else if (chan[1].equalsIgnoreCase("friendrequest")) {
			UUID from = UUID.fromString(msgParts[0]);
			UUID to = UUID.fromString(msgParts[1]);

			BungeeBridge.getInstance().getFriendsManagement().sendRequest(from, to);
		}
	}
}
