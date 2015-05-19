package net.zyuiop.bungeebridge.core.handlers;

import com.google.common.collect.HashMultimap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.zyuiop.bungeebridge.BungeeBridge;
import redis.clients.jedis.Client;
import redis.clients.jedis.JedisPubSub;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class MainSubscriber extends JedisPubSub implements Listener {

	protected HashMultimap<String, PubSubConsumer> consumers = HashMultimap.create();
	protected HashMultimap<String, PubSubConsumer> pconsumers = HashMultimap.create();

	public void addConsumer(String chan, PubSubConsumer consummer) {
		this.consumers.put(chan, consummer);
	}

	public void addPConsumer(String chan, PubSubConsumer consummer) {
		this.pconsumers.put(chan, consummer);
	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		ProxyServer.getInstance().getLogger().info("Subscribed on pattern " + pattern);
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		ProxyServer.getInstance().getLogger().info("Subscribed on channel " + channel);
	}

	@Override
	public void proceed(Client client, String... channels) {
		ProxyServer.getInstance().getScheduler().schedule(BungeeBridge.getInstance(), () -> {
			consumers.keySet().forEach(this::subscribe);
			pconsumers.keySet().forEach(this::psubscribe);
		}, 1, TimeUnit.SECONDS);

		super.proceed(client, channels);
	}

	@Override
	public void proceedWithPatterns(Client client, String... channels) {
		ProxyServer.getInstance().getScheduler().schedule(BungeeBridge.getInstance(), () -> {
			consumers.keySet().forEach(this::subscribe);
			pconsumers.keySet().forEach(this::psubscribe);
		}, 1, TimeUnit.SECONDS);

		super.proceedWithPatterns(client, channels);
	}

	@Override
	public void onMessage(String channel, String message) {
		Set<PubSubConsumer> consummers = this.consumers.get(channel);
		if (consummers != null)
			try {
				consummers.stream().forEach(consumer -> { consumer.consume(channel, message); ProxyServer.getInstance().getLogger().info("Consuming to " + consumer.getClass().getName()); });
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	// Debug

	private CopyOnWriteArraySet<UUID> debug = new CopyOnWriteArraySet<>();
	private boolean console = false;

	public void toggle(UUID uid) {
		if (debug.contains(uid))
			debug.remove(uid);
		else
			debug.add(uid);
	}

	public void toggle(CommandSender sender) {
		if (sender instanceof ProxiedPlayer)
			toggle(((ProxiedPlayer) sender).getUniqueId());
		else
			console = !console;
	}

	@EventHandler
	public void onLogout(PlayerDisconnectEvent event) {
		debug.remove(event.getPlayer().getUniqueId());
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		if (channel.equals("__sentinel__:hello"))
			return;

		if (pattern.equals("*")) {
			TextComponent debugMessage = new TextComponent("[ProxyDebug : " + channel + "] " + message);
			debugMessage.setColor(ChatColor.GREEN);

			for (UUID id : debug) {
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(id);
				if (player == null)
					debug.remove(id);
				else
					player.sendMessage(debugMessage);
			}

			if (console)
				ProxyServer.getInstance().getConsole().sendMessage(debugMessage);
		} else {
			Set<PubSubConsumer> consummers = this.pconsumers.get(pattern);
			if (consummers != null)
				try {
					consummers.stream().forEach(consumer -> { consumer.consume(channel, message); ProxyServer.getInstance().getLogger().info("Consuming to " + consumer.getClass().getName()); });
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
}
