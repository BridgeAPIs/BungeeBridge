package net.zyuiop.bungeebridge.interactions.friends;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;
import net.zyuiop.bungeebridge.utils.SettingsManager;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class FriendsManagement {


    /**
     * KEYS :
     * friendrequests:<uuid sender>:<uuid receiver>
     * friends:<uuid> : liste d'uuids
     */
    protected BungeeBridge plugin;

    public FriendsManagement(BungeeBridge plugin) {
        this.plugin = plugin;
		plugin.getConnector().psubscribe("friends.*", new FriendsConsumer(this));
		plugin.getProxy().getPluginManager().registerCommand(plugin, new FriendsCommand(this));
    }

    public String sendRequest(UUID from, UUID add) {

        if (from.equals(add))
            return ChatColor.RED + "Vous ne pouvez pas devenir ami de vous même.";

        String dbKey = "friendrequest:"+from+":"+add;
        String checkKey = "friendrequest:"+add+":"+from;

        if (isFriend(from, add)) {
            return ChatColor.RED+"Vous êtes déjà ami avec cette personne.";
        }

        Jedis jedis = plugin.getConnector().getResource();
        String value = jedis.get(dbKey);
        if (jedis.get(checkKey) != null) {
            jedis.close();
            return grantRequest(add, from);
        }

        if (value != null) {
            jedis.close();
            return ChatColor.RED+"Vous avez déjà envoyé une demande d'ami a cette personne. Merci d'attendre qu'elle y réponde.";
        }

        String allow = SettingsManager.getSetting(add, "friendsenabled");
        if (allow != null && allow.equals("false")) {
            jedis.close();
            return ChatColor.RED+"Cette personne n'autorise pas les demandes d'ami.";
        }

        if (!BungeeBridge.getInstance().getNetworkBridge().isOnline(add)) {
            jedis.close();
            return ChatColor.RED+"Ce joueur n'est pas en ligne.";
        }

        if (add == null) {
            jedis.close();
            return ChatColor.RED + "Une erreur s'est produite.";
        }

        FriendRequest request = new FriendRequest(from, add, new Date());
        jedis.set(dbKey, new Gson().toJson(request));
        jedis.close();

		BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("friends.request", from + " " + add + " " + System.currentTimeMillis()));

        return ChatColor.GREEN+"Demande d'ami envoyée.";
    }

    public boolean isFriend(UUID from, UUID isFriend) {
        return UUIDFriendList(from).contains(isFriend);
    }

    public String grantRequest(UUID from, UUID add) {

        if (from.equals(add))
            return ChatColor.RED + "Vous ne pouvez pas devenir ami de vous même.";

        if (isFriend(from, add))
            return ChatColor.RED+"Vous êtes déjà ami avec cette personne.";


        String dbKey = "friendrequest:"+from+":"+add;
        Jedis jedis = plugin.getConnector().getResource();
        String value = jedis.get(dbKey);
        if (value == null) {
            jedis.close();
            return ChatColor.RED+"Aucune demande d'ami correspondante.";
        }

        jedis.del(dbKey);

        if (add == null) {
            jedis.close();
            return ChatColor.RED + "Une erreur s'est produite.";
        }

        jedis.rpush("friends:"+from, add.toString());
        jedis.rpush("friends:"+add, from.toString());

        jedis.close();

		BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("friends.response", from + " " + add + " " + String.valueOf(true)));

        String pseudo = BungeeBridge.getInstance().getUuidTranslator().getName(from, false);

        return ChatColor.GREEN+"Vous êtes maintenant ami avec "+pseudo+".";
    }

    public String denyRequest(UUID from, UUID add) {
        String dbKey = "friendrequest:"+from+":"+add;
        Jedis jedis = plugin.getConnector().getResource();
        String value = jedis.get(dbKey);
        if (value == null) {
            jedis.close();
            return ChatColor.RED+"Aucune demande d'ami correspondante.";
        }

        jedis.del(dbKey);
        jedis.close();

		BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("friends.response", from + " " + add + " " + String.valueOf(false)));

		String pseudo = BungeeBridge.getInstance().getUuidTranslator().getName(from, false);

        return ChatColor.GREEN+"Vous avez refusé la demande d'ami de "+pseudo+".";
    }

    public String removeFriend(UUID asking, UUID askTo) {
        String dbKey = "friends:"+asking;
        String dbKeyTo = "friends:"+askTo;

        Jedis jedis = plugin.getConnector().getResource();
        boolean failed = (jedis.lrem(dbKey, 0, askTo.toString()) == 0 || jedis.lrem(dbKeyTo, 0, asking.toString()) == 0);
        jedis.close();
        if (failed)
            return ChatColor.RED+"Une erreur s'est produite : vous n'êtes pas ami(e) avec cette personne.";
        return ChatColor.GREEN+"Vous n'êtes plus ami(e) avec "+plugin.getUuidTranslator().getName(askTo, false)+".";
    }

    public ArrayList<String> friendList(UUID asking) {
        ArrayList<String> playerNames = new ArrayList<>();

        for (UUID id : UUIDFriendList(asking)) {
            String name = BungeeBridge.getInstance().getUuidTranslator().getName(id, false);
            if (name == null) {
                continue;
            }
            playerNames.add(name);
        }
        return playerNames;
    }

    public ArrayList<UUID> UUIDFriendList(UUID asking) {
        ArrayList<UUID> playerIDs = new ArrayList<>();

        Jedis jedis = plugin.getConnector().getResource();
        for (String data : jedis.lrange("friends:"+asking, 0, -1)) {
            if (data == null || data.equals("")) {
                jedis.lrem("friends:"+asking, 0, data);
                continue;
            }

            try  {
                UUID id = UUID.fromString(data);
				playerIDs.add(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        jedis.close();
        return playerIDs;
    }

    public HashMap<UUID, String> associatedFriendsList(UUID asking) {
        HashMap<UUID, String> ret = new HashMap<>();

        for (UUID id : UUIDFriendList(asking)) {
            String name = plugin.getUuidTranslator().getName(id, false);
            if (name == null) {
                continue;
            }
            ret.put(id, name);
        }
        return ret;
    }

    public HashMap<UUID, String> onlineAssociatedFriendsList(UUID asking) {
        HashMap<UUID, String> ret = new HashMap<>();
        HashMap<UUID, String> map = associatedFriendsList(asking);

		map.keySet().stream().filter(id -> BungeeBridge.getInstance().getNetworkBridge().isOnline(id)).forEach(id -> ret.put(id, map.get(id)));

        return ret;
    }

    public ArrayList<String> requestsList(UUID asking) {
        String dbKey = "friendrequest:*:"+asking;
        ArrayList<String> playerNames = new ArrayList<>();

        Jedis jedis = plugin.getConnector().getResource();
        for (String data : jedis.keys(dbKey)) {
            String[] parts = data.split(":");
            try  {
                UUID id = UUID.fromString(parts[1]);
                playerNames.add(plugin.getUuidTranslator().getName(id, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        jedis.close();
        return playerNames;
    }

    public ArrayList<String> sentRequestsList(UUID asking) {
        String dbKey = "friendrequest:"+asking+":";
        ArrayList<String> playerNames = new ArrayList<>();

        Jedis jedis = plugin.getConnector().getResource();
        for (String data : jedis.keys(dbKey)) {
            String[] parts = data.split(":");
            try  {
                UUID id = UUID.fromString(parts[1]);
				playerNames.add(plugin.getUuidTranslator().getName(id, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        jedis.close();
        return playerNames;
    }

	public void request(UUID from, UUID to, Date date) {
		ProxiedPlayer pl = ProxyServer.getInstance().getPlayer(to);
		if (pl == null)
			return;

		String pseudo = BungeeBridge.getInstance().getUuidTranslator().getName(from, false);
		if (pseudo == null)
			return;

		TextComponent line = new TextComponent(ChatColor.GOLD+"Vous avez reçu une demande d'ami de "+ChatColor.AQUA+pseudo+ChatColor.GOLD+" : ");
		TextComponent accept = new TextComponent("[Accepter]");
		accept.setColor(ChatColor.GREEN);
		accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends accept "+pseudo));
		accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN+"Accepter la demande d'ami").create()));
		TextComponent refuse = new TextComponent("[Refuser]");
		refuse.setColor(ChatColor.RED);
		refuse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends deny "+pseudo));
		refuse.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED+"Refuser la demande d'ami").create()));
		line.addExtra(accept);
		line.addExtra(new ComponentBuilder(" ou ").color(ChatColor.GOLD).create()[0]);
		line.addExtra(refuse);

		pl.sendMessage(line);
	}

	public void response(UUID from, UUID to, boolean accepted) {
		final ProxiedPlayer pl = ProxyServer.getInstance().getPlayer(from);
		if (pl == null)
			return;

		String pseudo = BungeeBridge.getInstance().getUuidTranslator().getName(to, false);
		if (pseudo == null)
			return;

		if (accepted) {
			pl.sendMessage(new ComponentBuilder(pseudo+" a accepté votre demande d'ami.").color(ChatColor.GREEN).create());
		} else {
			pl.sendMessage(new ComponentBuilder(pseudo+" a refusé votre demande d'ami.").color(ChatColor.RED).create());
		}

	}
}
