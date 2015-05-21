package net.bridgesapis.bungeebridge.core.proxies;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.core.database.Publisher;
import net.bridgesapis.bungeebridge.core.database.ServerSettings;
import net.bridgesapis.bungeebridge.core.players.PlayerData;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.bridgesapis.bungeebridge.utils.Misc;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.UUID;

public class BridgeListener implements Listener {

	private final NetworkBridge bridge;

	public BridgeListener(NetworkBridge bridge) {
		this.bridge = bridge;
	}

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		Jedis jedis = bridge.getPlugin().getConnector().getBungeeResource();
		boolean isOnline = false;
		for (String key : bridge.getProxies())
			if (!isOnline && jedis.sismember("connected:" + key, event.getPlayer().getUniqueId()+""))
				isOnline = true;

		if (isOnline) {
			event.getPlayer().disconnect(new ComponentBuilder(I18n.getTranslation("already_connected")).color(ChatColor.RED).create());
			return;
		}
		jedis.close();


		PlayerData data = bridge.getPlugin().getPlayerDataManager().getPlayerData(event.getPlayer().getUniqueId());
		data.set("currentproxy", bridge.getPlugin().getProxyName());
		data.setLong("lastaction", System.currentTimeMillis());

		bridge.getPlugin().getPublisher().publish(new Publisher.PendingMessage("proxybridge", "login " + event.getPlayer().getUniqueId() + " " + event.getPlayer().getName()));

		bridge.getPlugin().getExecutor().addTask(() -> {
			Jedis j = bridge.getPlugin().getConnector().getBungeeResource();
			j.sadd("connected:" + bridge.getPlugin().getProxyName(), "" + event.getPlayer().getUniqueId());
			bridge.getPlugin().getUuidTranslator().persistInfo(event.getPlayer().getDisplayName(), event.getPlayer().getUniqueId(), j);
			try {
				if (BungeeBridge.getInstance().getPermissionsBridge().getApi().getUser(event.getPlayer().getUniqueId()).hasPermission("staff.member"))
					j.sadd("staffonline", "" + event.getPlayer().getUniqueId());
			} catch (Exception ignored) {
			}

			j.close();
		});

	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		bridge.getPlugin().getPublisher().publish(new Publisher.PendingMessage("proxybridge", "logout " + event.getPlayer().getUniqueId() + " " + event.getPlayer().getName()));

		bridge.getPlugin().getExecutor().addTask(() -> {
			Jedis jedis = bridge.getPlugin().getConnector().getBungeeResource();
			jedis.srem("connected:" + bridge.getPlugin().getProxyName(), "" + event.getPlayer().getUniqueId());
			jedis.srem("staffonline", "" + event.getPlayer().getUniqueId());
			bridge.getPlugin().getUuidTranslator().persistInfo(event.getPlayer().getDisplayName(), event.getPlayer().getUniqueId(), jedis);
			jedis.close();

			jedis = bridge.getPlugin().getConnector().getResource();
			jedis.hincrBy("logintime", event.getPlayer().getUniqueId().toString(), System.currentTimeMillis() - Long.parseLong(jedis.hget("lastlogin", event.getPlayer().getUniqueId().toString())));
			jedis.srem("playersWithPack", event.getPlayer().getUniqueId().toString());
			jedis.close();

			PlayerData data = bridge.getPlugin().getPlayerDataManager().getPlayerData(event.getPlayer().getUniqueId());
			data.remove("currentserver");
			data.remove("currentproxy");
			bridge.getPlugin().getPlayerDataManager().unload(event.getPlayer().getUniqueId());

			if (BungeeBridge.getInstance().getPartiesManager() != null)
				BungeeBridge.getInstance().getPartiesManager().logout(event.getPlayer().getUniqueId());
		});
	}

	@EventHandler
	public void onLogin(final LoginEvent e) {
		e.registerIntent(bridge.getPlugin());
		final BungeeBridge plugin = bridge.getPlugin();

		try {
			final UUID id = e.getConnection().getUniqueId();
			final ServerSettings instance = bridge.getPlugin().getServerSettings();
			if (! instance.isAllowJoin()) {
				e.setCancelled(true);
				e.setCancelReason(ChatColor.RED + "Le serveur n'est pas prêt à accueillir des joueurs.");
				e.completeIntent(plugin);
				return;
			}

			if (e.getConnection().getVersion() < ProtocolConstants.MINECRAFT_1_8) {
				e.setCancelled(true);
				e.setCancelReason(ChatColor.RED + "Ce serveur nécessite Minecraft en version 1.8 minimum.");
				e.completeIntent(plugin);
				return;
			}

			// On charge l'utilisateur
			PermissionsAPI api = BungeeBridge.getInstance().getPermissionsBridge().getApi();
			PermissionUser user = api.getUser(id);

			if (instance.getType().equals(ServerSettings.CloseType.CLOSED)) {
				if (! user.hasPermission("netjoin.closed")) {
					e.setCancelled(true);
					e.setCancelReason(ChatColor.RED + "Le serveur est en maintenance.");
					try {
						e.getConnection().disconnect(new ComponentBuilder("Le serveur est en maintenance.").color(ChatColor.RED).create());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					e.completeIntent(plugin);
					return;
				}
			}

			if (instance.getType().equals(ServerSettings.CloseType.CLOSED)) {
				if (! user.hasPermission("netjoin.closed") || ! user.hasPermission("netjoin.vip")) {
					e.setCancelled(true);
					e.setCancelReason(ChatColor.RED + "Accès réservé aux " + ChatColor.GREEN + "VIP");
					try {
						e.getConnection().disconnect(new ComponentBuilder(ChatColor.RED + "Accès réservé aux " + ChatColor.GREEN + "VIP").create());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					e.completeIntent(plugin);
					return;
				}
			}

			// Serveur ouvert a tous les joueurs //
			int joueurs = BungeeBridge.getInstance().getNetworkBridge().getPlayersOnline().size();
			if (joueurs >= instance.getMaxPlayers()) {
				if (! user.hasPermission("netjoin.full")) {
					e.setCancelled(true);
					TextComponent reason = new TextComponent("Le serveur est plein. Devenez ");
					reason.setColor(ChatColor.RED);
					reason.addExtra(new ComponentBuilder("VIP ").color(ChatColor.GREEN).create()[0]);
					reason.addExtra(new ComponentBuilder("pour vous connecter quand même.").color(ChatColor.RED).create()[0]);
					e.setCancelReason(reason.getText());
					try {
						e.getConnection().disconnect(reason);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					e.completeIntent(plugin);
					return;
				}
			}

			Jedis jedis = plugin.getConnector().getResource();
			String ban = jedis.get("banlist:reason:" + id); //Requète tout le temps
			if (ban != null) {

				long ttl = jedis.ttl("banlist:reason:" + id); // Requête tout le temps
				String duration = "définitivement";
				if (ttl >= 0) {
					duration = Misc.formatTime(ttl);
				}
				TextComponent reason = new TextComponent("Vous êtes banni " + duration + ". \nMotif : " + ban);
				reason.setColor(ChatColor.RED);

				e.setCancelReason(ChatColor.RED + "Vous êtes banni " + duration + ". \nMotif : " + ban);
				e.setCancelled(true);
				try {
					e.getConnection().disconnect(reason);
				} catch (Exception ex) {

				}
				jedis.close();
				e.completeIntent(plugin);
				return;
			}

			jedis.sadd("uniqueplayers", e.getConnection().getUniqueId().toString());
			jedis.hset("lastlogin", e.getConnection().getUniqueId().toString(), System.currentTimeMillis() + "");

			String muted = jedis.get("mute:" + id);
			if (muted != null) {
				String reason = jedis.get("mute:" + id + ":reason");
				Long end = Long.decode(muted);
				Date fin = new Date(end);
				if (fin.before(new Date())) {
					jedis.del("mute:" + id);
					jedis.del("mute:" + id + ":reason");
				} else {
					plugin.getChatListener().addMute(id, fin, reason);
				}
			}

			jedis.close();

		} catch (Exception ex) {
			e.setCancelled(true);
			e.setCancelReason(ChatColor.RED + "Une erreur s'est produite durant votre connexion : "+ex.getMessage());
		}
		e.completeIntent(plugin);
	}

	@EventHandler
	public void onChangeServer(ServerConnectedEvent event) {
		bridge.getPlugin().getExecutor().addTask(() -> {
			PlayerData data = bridge.getPlugin().getPlayerDataManager().getPlayerData(event.getPlayer().getUniqueId());
			data.set("currentserver", event.getServer().getInfo().getName());
			data.setLong("lastaction", System.currentTimeMillis());
		});

		bridge.getPlugin().getPublisher().publish(new Publisher.PendingMessage("proxybridge", "move " + event.getPlayer().getUniqueId() + " " + event.getServer().getInfo().getName()));
	}

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		try {
			Jedis jedis = bridge.getPlugin().getConnector().getBungeeResource();
			long co = 0;
			for (String key : bridge.getProxies()) {
				Long val = jedis.scard("connected:" + key);
				if (val != null)
					co += val;
			}
			jedis.close();

			ServerSettings serverSettings = bridge.getPlugin().getServerSettings();

			ServerPing ping = event.getResponse();
			ServerPing.Players players = ping.getPlayers();
			players.setMax(serverSettings.getMaxPlayers());
			players.setOnline((int) co);
			ping.setPlayers(players);

			ping.setDescription(serverSettings.getServerLine() + "\n" + serverSettings.getMotd());
			event.setResponse(ping);
		} catch (Exception e) {
			ServerPing ping = event.getResponse();
			ping.setPlayers(new ServerPing.Players(0, 0, new ServerPing.PlayerInfo[]{}));
			ping.setDescription(ChatColor.RED + "[X] Network Difficulties [X] \n > The server is currently down.");
			event.setResponse(ping);
		}
	}

}
