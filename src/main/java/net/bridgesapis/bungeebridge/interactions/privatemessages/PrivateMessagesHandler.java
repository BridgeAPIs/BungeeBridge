package net.bridgesapis.bungeebridge.interactions.privatemessages;

import com.google.gson.Gson;
import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;

public class PrivateMessagesHandler implements PubSubConsumer {

	private final PrivateMessagesManager manager;

	public PrivateMessagesHandler(PrivateMessagesManager manager) {
		this.manager = manager;
	}

	@Override
	public void consume(String channel, String message) {
		PrivateMessage privateMessage = new Gson().fromJson(message, PrivateMessage.class);
		manager.receive(privateMessage);
	}
}
