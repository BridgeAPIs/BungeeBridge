package net.bridgesapis.bungeebridge.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.database.Publisher;
import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;
import net.bridgesapis.bungeebridge.utils.Misc;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatListener implements Listener, PubSubConsumer {

    protected BungeeBridge plugin;
	public CopyOnWriteArraySet<String> blacklist = new CopyOnWriteArraySet<>();
    protected ConcurrentHashMap<UUID, MessageData> lastMessages = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<UUID, Date> mutedPlayers = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<UUID, String> muteReasons = new ConcurrentHashMap<>();

    public ChatListener(BungeeBridge plugin) {
        this.plugin = plugin;
    }

	@EventHandler
    public void onChat(ChatEvent event) {
        if (event.getMessage().startsWith("/"))
            return;

        Connection connection = event.getSender();
        if (!(connection instanceof ProxiedPlayer))
            return;

        String message = event.getMessage();
        long time = System.currentTimeMillis();
        ProxiedPlayer player = (ProxiedPlayer) connection;

        if (mutedPlayers.containsKey(player.getUniqueId())) {
            Date end = mutedPlayers.get(player.getUniqueId());
            if (end.before(new Date())) {
				mutedPlayers.remove(player.getUniqueId());
                muteReasons.remove(player.getUniqueId());
            } else {
                player.sendMessage(new ComponentBuilder("Vous êtes actuellement muet pour une durée de " + Misc.formatTime((end.getTime() - System.currentTimeMillis()) / 1000)).color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder("Raison : " + ChatColor.YELLOW + muteReasons.get(player.getUniqueId())).color(ChatColor.RED).create());
                event.setCancelled(true);
                return;
            }
        }

        if (BungeeBridge.getInstance().getPartiesManager() != null && message.startsWith("*")) {
            UUID party = plugin.getPartiesManager().getPlayerParty(player.getUniqueId());
            if (party != null) {
                message = message.substring(1);
                message = message.trim();
                event.setCancelled(true);
                plugin.getPublisher().publish(new Publisher.PendingMessage("parties.message", party + " " + player.getName() + " " + message));
                return;
            }
        }

        if (plugin.getPermissionsBridge().getApi().getUser(player.getUniqueId()).hasPermission("chatrestrict.ignore")) {
            return;
        }

        MessageData last = lastMessages.get(((ProxiedPlayer) connection).getUniqueId());
        if (last != null) {
            if (last.isTooEarly(time)) {
                ((ProxiedPlayer) connection).sendMessage(ChatColor.RED + "Merci de ne pas envoyer de messages trop souvent.");
                event.setCancelled(true);
                return;
            } else if (last.isSame(message, time)) {
                ((ProxiedPlayer) connection).sendMessage(ChatColor.RED + "Merci de ne pas envoyer plusieurs fois le même message.");
                event.setCancelled(true);
                return;
            }
        }

        MessageData current = new MessageData();
        current.message = message;
        current.time = time;
        if (last != null) {
			lastMessages.replace(((ProxiedPlayer) connection).getUniqueId(), current);
		} else {
			lastMessages.put(((ProxiedPlayer) connection).getUniqueId(), current);
		}

        if (message.matches("[0-9]\\.[0-9]\\.[0-9]\\.[0-9]")) {
            ((ProxiedPlayer) connection).sendMessage(ChatColor.RED + "Pas d'ips dans le chat !");
            event.setCancelled(true);
        } else if (message.matches("[a-zA-Z]\\.[a-zA-Z]\\.[a-zA-Z]")) {
            ((ProxiedPlayer) connection).sendMessage(ChatColor.RED + "Pas d'ips dans le chat !");
            event.setCancelled(true);
        } else if (message.matches("[A-Z]{4,}")) {
            ((ProxiedPlayer) connection).sendMessage(ChatColor.RED + "Pas de messages en majuscules !");
            event.setCancelled(true);
        } else if (message.length() < 2) {
            ((ProxiedPlayer) connection).sendMessage(ChatColor.RED + "Quand on a rien a dire, il faut savoir se taire.");
            event.setCancelled(true);
        }

		String check = message.toLowerCase();

		for (String w : blacklist) {
			if (check.startsWith(w) || check.endsWith(" " + w) || check.contains(" " + w + " ")) {
				event.setCancelled(true);
				((ProxiedPlayer) connection).sendMessage(new ComponentBuilder("Votre message contient un mot interdit.").color(ChatColor.RED).create());
				return;
			}
		}
    }

	public void addMute(UUID id, Date end, String reason) {
		if (!mutedPlayers.containsKey(id))
			mutedPlayers.put(id, end);
		if (!muteReasons.containsKey(id))
			muteReasons.put(id, reason);
	}

	public void removeMute(UUID id) {
		if (mutedPlayers.containsKey(id))
			mutedPlayers.remove(id);
		if (muteReasons.containsKey(id))
			muteReasons.remove(id);
	}

	public boolean isMuted(UUID id) {
		if (mutedPlayers.containsKey(id)) {
			Date end = mutedPlayers.get(id);
			if (end.before(new Date())) {
				removeMute(id);
			} else {
				return true;
			}
		}
		return false;
	}

	public String getReason(UUID id) {
		return muteReasons.get(id);
	}

	public Date getEnd(UUID id) {
		return mutedPlayers.get(id);
	}

    @Override
    public void consume(String channel, String message) {
        if (channel.equals("mute.add")) {
            String[] parts = message.split(" ");
            UUID id = UUID.fromString(parts[0]);
            long end = Long.valueOf(parts[1]);
            String reason = StringUtils.join(Arrays.copyOfRange(parts, 2, parts.length), " ");

            if (ProxyServer.getInstance().getPlayer(id) != null) {
                mutedPlayers.put(id, new Date(end));
                muteReasons.put(id, reason);
            }
        } else if (channel.equals("mute.remove")) {
            UUID id = UUID.fromString(message);
            mutedPlayers.remove(id);
            muteReasons.remove(id);
        }
    }

    public class MessageData {

        public String message = "";
        public long time = 0;

        public boolean isSame(String message, long time) {
            boolean eq = this.message.equals(message);
            if (!eq)
                return false;
            return (this.time + 15000 > time); // 15 secondes entre chaque message identique
        }

        public boolean isTooEarly(long time) {
            if (this.time + 1500 > time)
                return true;
            return false;
        }
    }
}
