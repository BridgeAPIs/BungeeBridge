package net.zyuiop.bungeebridge.interactions.privatemessages;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;
import net.zyuiop.bungeebridge.utils.Misc;
import net.zyuiop.bungeebridge.utils.SettingsManager;
import net.zyuiop.bungeebridge.utils.UnknownPlayer;

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
			TextComponent msg = new TextComponent("Le pseudo entré est invalide.");
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		UUID id = plugin.getUuidTranslator().getUUID(player, false);
		if (id == null) {
			TextComponent msg = new TextComponent("Le pseudo entré est invalide.");
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		send(sender, player, id, message);
	}

	public void reply(ProxiedPlayer sender, String message) {
		UUID to = getReplyTo(sender.getUniqueId());
		if (to == null) {
			TextComponent msg = new TextComponent("Vous n'avez personne à qui répondre.");
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}
		send(sender, plugin.getUuidTranslator().getName(to, false), to, message);
	}

	public void send(ProxiedPlayer sender, String name, UUID id, String message) {
		if (plugin.getChatListener().isMuted(sender.getUniqueId())) {
			sender.sendMessage(new ComponentBuilder("Vous êtes actuellement muet pour une durée de " + Misc.formatTime((plugin.getChatListener().getEnd(sender.getUniqueId()).getTime() - System.currentTimeMillis()) / 1000)).color(ChatColor.RED).create());
			sender.sendMessage(new ComponentBuilder("Raison : " + ChatColor.YELLOW + plugin.getChatListener().getReason(sender.getUniqueId())).color(ChatColor.RED).create());
			return;
		}

		if(!plugin.getNetworkBridge().isOnline(id)) {
			TextComponent msg = new TextComponent("Le joueur que vous recherchez n'est pas connecté.");
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		String allowPMS = SettingsManager.getSetting(id, "mpsenabled");
		boolean bypass = plugin.getFriendsManagement().isFriend(sender.getUniqueId(), id);
		if (!bypass && sender.hasPermission("mp.force"))
			bypass = true;

		if (allowPMS != null && allowPMS.equals("false") && !bypass) {
			TextComponent msg = new TextComponent("Vous ne pouvez pas envoyer de MP car vous les avez désactivés.");
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}

		String allowPM = SettingsManager.getSetting(id, "mpsenabled");
		if (allowPM != null && allowPM.equals("false") && !bypass) {
			TextComponent msg = new TextComponent("Le joueur refuse les MPs.");
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
			TextComponent msg = new TextComponent("Une erreur s'est produite.");
			msg.setColor(ChatColor.RED);
			sender.sendMessage(msg);
			return;
		}
	}

	/**
	 * Message privé entre deux personnes
	 * @param message
	 */
	public void receive(PrivateMessage message) {
		ProxiedPlayer from = plugin.getProxy().getPlayer(message.getSender().getPlayerId());
		ProxiedPlayer to = plugin.getProxy().getPlayer(message.getReceiver().getPlayerId());

		if (from != null) {
			// Envoi du message //
			lastMessageSender.put(from.getUniqueId(), message.getReceiver().getPlayerId());
			TextComponent msg = new TextComponent(ChatColor.GOLD + "Message envoyé à ");
			TextComponent recv = new TextComponent(message.getReceiver().getPlayerName());
			recv.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg "+message.getReceiver().getPlayerName()+" "));
			recv.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD+"Cliquez pour lui envoyer un MP").create()));
			recv.setColor(ChatColor.AQUA);

			msg.addExtra(recv);
			msg.addExtra(ChatColor.GOLD + " : ");

			TextComponent content = new TextComponent(message.getMessage());
			content.setColor(ChatColor.GREEN);
			content.setItalic(true);

			msg.addExtra(content);

			from.sendMessage(msg);
		}

		if (to != null) {
			// Envoi du message //
			lastMessageSender.put(to.getUniqueId(), message.getSender().getPlayerId());
			TextComponent msg = new TextComponent(ChatColor.GOLD + "Message reçu de ");
			TextComponent sender = new TextComponent(message.getSender().getPlayerName());
			sender.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + message.getSender().getPlayerName()+" "));
			sender.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + "Cliquez pour lui envoyer un MP").create()));
			sender.setColor(ChatColor.AQUA);

			msg.addExtra(sender);
			msg.addExtra(ChatColor.GOLD + " : ");

			TextComponent content = new TextComponent(message.getMessage());
			content.setColor(ChatColor.GREEN);
			content.setItalic(true);

			msg.addExtra(content);

			to.sendMessage(msg);
		}

		return;
	}
}
