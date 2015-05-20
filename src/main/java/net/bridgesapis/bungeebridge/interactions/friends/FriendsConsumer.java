package net.bridgesapis.bungeebridge.interactions.friends;

import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.UUID;

public class FriendsConsumer implements PubSubConsumer {

	private final FriendsManagement management;

	public FriendsConsumer(FriendsManagement management) {
		this.management = management;
	}

	@Override
	public void consume(String channel, String message) {
		String[] parts = StringUtils.split(channel, ".");
		if (parts.length < 2)
			return;

		String[] data = StringUtils.split(message, " ");
		if (data.length < 3)
			return;

		String operation = parts[1];
		UUID from = UUID.fromString(data[0]);
		UUID to = UUID.fromString(data[1]);
		if (operation.equals("request")) {
			Date val = new Date(Long.valueOf(data[2]));
			management.request(from, to, val);
		} else if (operation.equals("response")) {
			Boolean val = Boolean.valueOf(data[2]);
			management.response(from, to, val);
		}
	}
}
