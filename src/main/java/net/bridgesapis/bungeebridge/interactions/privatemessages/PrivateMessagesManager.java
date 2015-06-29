package net.bridgesapis.bungeebridge.interactions.privatemessages;

import com.google.gson.Gson;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.bridgesapis.bungeebridge.utils.Misc;
import net.bridgesapis.bungeebridge.utils.SettingsManager;
import net.bridgesapis.bungeebridge.utils.UnknownPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.bridgesapis.bungeebridge.core.database.Publisher;

import java.util.HashMap;
import java.util.UUID;

public class PrivateMessagesManager {

	private BungeeBridge plugin;
	private HashMap<UUID, UUID> lastMessageSender = new HashMap<>();

	public PrivateMessagesManager(BungeeBridge plugin) {
		this.plugin = plugin;
	}

	public UUID getReplyTo(UUID player) {
		return lastMessageSender.get(player);
	}

	public void send(ProxiedPlayer sender, String player, String message) {
		if (player.length() < 4 || player.contains(".") || player.contains("-") || player.length() > 16) {
			TextComponent msg = new TextComponent(I18n.getTranslation("commands.locate.invalid"));
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		UUID id = plugin.getUuidTranslator().getUUID(player, false);
		if (id == null) {
			TextComponent msg = new TextComponent(I18n.getTranslation("commands.locate.invalid"));
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		send(sender, player, id, message);
	}

	public void reply(ProxiedPlayer sender, String message) {
		UUID to = getReplyTo(sender.getUniqueId());
		if (to == null) {
			TextComponent msg = new TextComponent(I18n.getModuleTranslation("messages", "commands.reply.noone_to_answer"));
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}
		send(sender, plugin.getUuidTranslator().getName(to, false), to, message);
	}

	public void send(ProxiedPlayer sender, String name, UUID id, String message) {
		if (plugin.getChatListener().isMuted(sender.getUniqueId())) {
			// TODO : Translate mute message
			sender.sendMessage(new ComponentBuilder("Vous êtes actuellement muet pour une durée de " + Misc.formatTime((plugin.getChatListener().getEnd(sender.getUniqueId()).getTime() - System.currentTimeMillis()) / 1000)).color(ChatColor.RED).create());
			sender.sendMessage(new ComponentBuilder("Raison : " + ChatColor.YELLOW + plugin.getChatListener().getReason(sender.getUniqueId())).color(ChatColor.RED).create());
			return;
		}

		if(!plugin.getNetworkBridge().isOnline(id)) {
			TextComponent msg = new TextComponent(I18n.getTranslation("commands.locate.offline"));
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		String allowPMS = SettingsManager.getSetting(id, "mpsenabled");
		boolean bypass = (plugin.hasFriends() && plugin.getFriendsManagement().isFriend(sender.getUniqueId(), id));
		if (!bypass && sender.hasPermission("mp.force"))
			bypass = true;

		if (allowPMS != null && allowPMS.equals("false") && !bypass) {
			TextComponent msg = new TextComponent(I18n.getModuleTranslation("messages", "errors.self_pm_disabled"));
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		String allowPM = SettingsManager.getSetting(id, "mpsenabled");
		if (allowPM != null && allowPM.equals("false") && !bypass) {
			TextComponent msg = new TextComponent(I18n.getModuleTranslation("messages", "errors.other_pm_disabled"));
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		try {
			UnknownPlayer from = new UnknownPlayer(sender.getUniqueId(), sender.getName());
			UnknownPlayer to = new UnknownPlayer(id, name);

			PrivateMessage mp = new PrivateMessage(from, to, message);
			String serialized = new Gson().toJson(mp);

			// Envoi du MP //
			plugin.getPublisher().publish(new Publisher.PendingMessage("privatemessages", serialized));
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendMessage(TextComponent.fromLegacyText(I18n.getTranslation("commands.misc.error_occured")));
		}
	}

	/**
	 * Receive a message from pubsub
	 * @param message The message to display
	 */
	public void receive(PrivateMessage message) {
		ProxiedPlayer from = plugin.getProxy().getPlayer(message.getSender().getPlayerId());
		ProxiedPlayer to = plugin.getProxy().getPlayer(message.getReceiver().getPlayerId());

		if (from != null) {
			// Envoi du message //
			lastMessageSender.put(from.getUniqueId(), message.getReceiver().getPlayerId());
			String preMsg = I18n.getModuleTranslation("messages", "display.sent")
					.replace("%NAME%", message.getReceiver().getPlayerName())
					.replace("%VIEWER%", from.getName());

			ChatColor color;
			try {
				color = ChatColor.valueOf(I18n.getModuleTranslation("messages", "display.sent_color"));
			} catch (Exception e) {
				color = ChatColor.GRAY;
			}

			boolean italic = true;
			try {
				italic = Boolean.valueOf(I18n.getModuleTranslation("messages", "display.sent_italic"));
			} catch (Exception ignored) {}

			TextComponent msg = new TextComponent(preMsg);
			TextComponent content = new TextComponent(message.getMessage());
			content.setColor(color);
			content.setItalic(italic);

			msg.addExtra(content);

			msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg "+message.getReceiver().getPlayerName()+" "));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(I18n.getModuleTranslation("messages", "display.answer_hover")).create()));

			from.sendMessage(msg);
		}

		if (to != null) {
			// Envoi du message //
			lastMessageSender.put(to.getUniqueId(), message.getSender().getPlayerId());
			String preMsg = I18n.getModuleTranslation("messages", "display.received")
					.replace("%NAME%", message.getSender().getPlayerName())
					.replace("%VIEWER%", to.getName());

			ChatColor color;
			try {
				color = ChatColor.valueOf(I18n.getModuleTranslation("messages", "display.received_color"));
			} catch (Exception e) {
				color = ChatColor.GRAY;
			}

			boolean italic = true;
			try {
				italic = Boolean.valueOf(I18n.getModuleTranslation("messages", "display.received_italic"));
			} catch (Exception ignored) {}

			TextComponent msg = new TextComponent(preMsg);
			TextComponent content = new TextComponent(message.getMessage());
			content.setColor(color);
			content.setItalic(italic);

			msg.addExtra(content);

			msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + message.getReceiver().getPlayerName() + " "));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(I18n.getModuleTranslation("messages", "display.answer_hover")).create()));

			to.sendMessage(msg);
		}
	}
}
