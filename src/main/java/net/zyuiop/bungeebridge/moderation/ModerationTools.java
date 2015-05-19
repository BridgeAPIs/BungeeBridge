package net.zyuiop.bungeebridge.moderation;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.permissions.PermissionsBridge;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class ModerationTools {
    public static void addSanction(JsonCaseLine sanction, UUID player) {
        Long time = System.currentTimeMillis();
        sanction.setTimestamp(time);
        String json = new Gson().toJson(sanction);
        Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
        jedis.zadd("case:"+player, time, json);
       	jedis.close();
    }

	public static boolean canSanction(UUID player) {
		PermissionsBridge bridge = BungeeBridge.getInstance().getPermissionsBridge();
		return ! ((bridge.getApi().getUser(player) != null && bridge.getApi().getUser(player).hasPermission("mod.donttouchme")));
	}

	public static TextComponent getCaseData(UUID player, String name) {
		Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
		long count = jedis.zcount("case:"+player, 0, Long.MAX_VALUE);
		jedis.close();

		TextComponent ret = new TextComponent("Le joueur "+name+" a "+count+" lignes dans son dossier. ");
		ret.setColor(ChatColor.RED);

		TextComponent url = new TextComponent("[Voir son dossier]");
		url.setColor(ChatColor.AQUA);
		url.setUnderlined(true);
		url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://samagames.net/modo/index.php/lookup/show/"+name));

		if (count > 10) {
			ret.setColor(ChatColor.RED);
		} else if (count > 5) {
			ret.setColor(ChatColor.GOLD);
		} else if (count > 0) {
			ret.setColor(ChatColor.GREEN);
		} else {
			return null;
		}

		ret.addExtra(url);

		return ret;
	}

	public static void checkFile(UUID player, String name, CommandSender moderator) {
		TextComponent message = getCaseData(player, name);
		if (message != null)
			moderator.sendMessage(message);
	}
}
