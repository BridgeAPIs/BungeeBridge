package net.bridgesapis.bungeebridge.core.handlers;

public interface PubSubConsumer {
	public void consume(String channel, String message);
}
