package net.zyuiop.bungeebridge.moderation;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;

public class JsonModMessage {

	protected String sender;
	protected ChatColor senderPrefix;
	protected String message;

	public JsonModMessage() {
	}

	public JsonModMessage(String sender, ChatColor senderPrefix, String message) {
		this.sender = sender;
		this.senderPrefix = senderPrefix;
		this.message = message;
	}

	public static JsonModMessage build(CommandSender sender, String message) {
		if (sender instanceof ProxiedPlayer) {
			PermissionUser user = BungeeBridge.getInstance().getPermissionsBridge().getApi().getUser(((ProxiedPlayer) sender).getUniqueId());
			String prefix = user.getProperty("prefix");
			ChatColor pr = ChatColor.getByChar(prefix.charAt(prefix.length() - 1));

			return new JsonModMessage(sender.getName(), pr, message);
		} else {
			return new JsonModMessage(sender.getName(), ChatColor.AQUA, message);
		}
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public ChatColor getSenderPrefix() {
		return senderPrefix;
	}

	public void setSenderPrefix(ChatColor senderPrefix) {
		this.senderPrefix = senderPrefix;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void send() {
		BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("moderationchan" , new Gson().toJson(this)));
	}
}
