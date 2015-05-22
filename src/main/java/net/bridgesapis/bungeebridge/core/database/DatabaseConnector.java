package net.bridgesapis.bungeebridge.core.database;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.handlers.MainSubscriber;
import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;
import net.bridgesapis.bungeebridge.core.handlers.SubscribingThread;
import redis.clients.jedis.Jedis;

/**
 * Created by zyuiop on 22/05/15.
 * Licensed under GNU LGPL license
 */
public abstract class DatabaseConnector {

	protected String password;
	protected BungeeBridge plugin;
	protected MainSubscriber commandsSubscriber;
	protected SubscribingThread thread;

	public void subscribe(String channel, PubSubConsumer consummer) {
		commandsSubscriber.subscribe(channel);
		commandsSubscriber.addConsumer(channel, consummer);
	}

	public void psubscribe(String channel, PubSubConsumer consummer) {
		commandsSubscriber.psubscribe(channel);
		commandsSubscriber.addPConsumer(channel, consummer);
	}

	public abstract Jedis getResource();

	public abstract Jedis getBungeeResource();

	public abstract void killConnections();

	public abstract void initiateConnections() throws InterruptedException;

	public abstract MainSubscriber getCommandsSubscriber();

	public abstract void disable();
}
