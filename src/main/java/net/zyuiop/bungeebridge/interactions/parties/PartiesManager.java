package net.zyuiop.bungeebridge.interactions.parties;

import com.google.common.collect.HashMultimap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PartiesManager {

	private final BungeeBridge bungeeBridge;
	private final HashMultimap<UUID, UUID> invited = HashMultimap.create();
	private final HashSet<UUID> disconnectedPlayers = new HashSet<>();

	public void forceLeaveAll() {
		for (UUID id : disconnectedPlayers)
			leave(id);
	}

	public PartiesManager(BungeeBridge bungeeBridge) {
		this.bungeeBridge = bungeeBridge;
		PartiesConsumer consumer = new PartiesConsumer(this);
		bungeeBridge.getConnector().psubscribe("parties.*", consumer);
	}

	public void invite(UUID inviter, UUID invited) {
		this.invited.put(invited, inviter);
		ProxyServer.getInstance().getScheduler().schedule(bungeeBridge, () -> this.invited.remove(invited, inviter), 1, TimeUnit.MINUTES);
	}

	public void accept(ProxiedPlayer player) {
		Set<UUID> invites = invited.get(player.getUniqueId());
		if (invites == null || invites.size() == 0) {
			player.sendMessage(new ComponentBuilder("Erreur : vous n'avez aucune invitation en attente.").color(ChatColor.RED).create());
		} else if (invites.size() > 1) {
			player.sendMessage(new ComponentBuilder("Erreur : vous avez plusieurs invitations en attente. Merci d'indiquer un pseudo.").color(ChatColor.RED).create());
		} else {
			accept(player, invites.iterator().next());
		}
	}

	public void accept(ProxiedPlayer player, String target) {
		UUID p = bungeeBridge.getUuidTranslator().getUUID(target, false);
		if (p == null) {
			player.sendMessage(new ComponentBuilder("Erreur : ce nom d'utilisateur n'existe pas.").color(ChatColor.RED).create());
		} else {
			accept(player, p);
		}
	}

	public void accept(ProxiedPlayer player, UUID invitedBy) {
		Set<UUID> invites = invited.get(player.getUniqueId());
		if (invites == null || !invites.contains(invitedBy)) {
			player.sendMessage(new ComponentBuilder("Erreur : vous n'avez aucune invitation de cette personne.").color(ChatColor.RED).create());
		} else {
			// Acceptatiooooooooon
			// A. Récupération de l'ID de party
			UUID party = getOrCreatePartyId(invitedBy);

			// B. Ajout du joueur
			addPlayerToParty(player.getUniqueId(), player.getName(), party);

			// C. Diffusion
			player.sendMessage(new ComponentBuilder("[Party] ").color(ChatColor.DARK_GREEN).append("Vous avez rejoint la party !").color(ChatColor.GREEN).create());
			bungeeBridge.getPublisher().publish(new Publisher.PendingMessage("parties.join", party + " " + player.getUniqueId() + " " + player.getName()));
		}
	}

	/**
	 * Returns the partyID for the player or create a new party with this player as a leader
	 * @param player
	 * @return
	 */
	public UUID getOrCreatePartyId(UUID player) {
		UUID party = getPlayerParty(player);
		if (party == null) {
			party = UUID.randomUUID();
			if (!bungeeBridge.getNetworkBridge().isOnline(player))
				return party;

			Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
			jedis.set("currentparty:" + player, party.toString());
			jedis.set("party:" + party + ":lead", player.toString());
			String name = bungeeBridge.getUuidTranslator().getName(player, false);
			if (name != null)
				jedis.hset("party:" + party + ":members", player.toString(), name);

			ServerInfo info = bungeeBridge.getServersManager().getServer(player);
			if (info != null)
				jedis.set("party:" + party + ":server", info.getName());

			jedis.close();

			return party;
		} else {
			return party;
		}
	}

	public void addPlayerToParty(UUID id, String name, UUID party) {
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		jedis.set("currentparty:" + id, party.toString());
		jedis.hset("party:" + party + ":members", id.toString(), name);
		jedis.close();
	}

	public UUID getPlayerParty(UUID player) {
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		String val = jedis.get("currentparty:" + player);
		jedis.close();
		return (val != null) ? UUID.fromString(val) : null;
	}

	public HashMap<UUID, String> getPlayersInParty(UUID party) {
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		Map<String, String> data = jedis.hgetAll("party:" + party + ":members");
		jedis.close();

		if (data == null)
			return new HashMap<>();

		HashMap<UUID, String> ret = new HashMap<>();
		data.entrySet().forEach(entry -> ret.put(UUID.fromString(entry.getKey()), entry.getValue()));

		return ret;
	}

	public ServerInfo getCurrentServer(UUID party) {
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		String server = jedis.get("party:" + party + ":server");
		jedis.close();
		return ProxyServer.getInstance().getServerInfo(server);
	}

	public UUID getLeader(UUID party) {
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		String leader = jedis.get("party:" + party + ":lead");
		jedis.close();
		return UUID.fromString(leader);
	}

	public void logout(UUID player) {
		UUID party;
		if ((party = getPlayerParty(player)) != null) {
			if (!disconnectedPlayers.contains(player)) {
				disconnectedPlayers.add(player);
				ProxyServer.getInstance().getScheduler().schedule(bungeeBridge, () -> {
					if (disconnectedPlayers.contains(player)) {
						disconnectedPlayers.remove(player);
						leave(player);
					}
				}, 5, TimeUnit.MINUTES);
				BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("parties.disconnect", party + " " + BungeeBridge.getInstance().getUuidTranslator().getName(player, false)));
			}
		}
	}

	public void leave(UUID player) {
		UUID party = getPlayerParty(player);
		if (party != null)
			leave(player, party);
	}

	public void leave(UUID player, UUID party) {
		BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("parties.leave", party + " " + player + " " + bungeeBridge.getUuidTranslator().getName(player, true)));
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		jedis.del("currentparty:" + player);
		jedis.hdel("party:" + party + ":members", "" + player);

		Set<UUID> members = getPlayersInParty(party).keySet();
		if (members.size() < 2) {
			BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("parties.disband", members.iterator().next()+""));
			jedis.del("party:" + party + ":server");
			jedis.del("party:" + party + ":lead");
			jedis.del("party:" + party + ":members");
			jedis.del("currentparty:" + members.iterator().next().toString());
		} else if (getLeader(party).equals(player)) {
			UUID leader = members.iterator().next();
			jedis.set("party:" + party + ":lead", leader.toString());

			try {
				String name = bungeeBridge.getUuidTranslator().getName(leader, false);
				BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("parties.lead", party + " " + name));
			} catch (Exception ignored) {}
		}

		jedis.close();
	}

	public void kick(UUID player, UUID party) {
		BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("parties.kick", party + " " + player + " " + bungeeBridge.getUuidTranslator().getName(player, true)));
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		jedis.del("currentparty:" + player);
		jedis.hdel("party:" + party + ":members", "" + player);

		Set<UUID> members = getPlayersInParty(party).keySet();
		if (members.size() < 2) {
			BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("parties.disband", members.iterator().next()+""));
			jedis.del("party:" + party + ":server");
			jedis.del("party:" + party + ":lead");
			jedis.del("party:" + party + ":members");
			jedis.del("currentparty:" + members.iterator().next().toString());
		}

		jedis.close();
	}

	public void setLeader(UUID party, UUID leader) {
		Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
		jedis.set("party:" + party + ":lead", leader.toString());
		jedis.close();
	}

	public void comeBack(UUID player) {
		UUID party;
		if ((party = getPlayerParty(player)) != null) {
			disconnectedPlayers.remove(player);
			BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("parties.comeback", party + " " + BungeeBridge.getInstance().getUuidTranslator().getName(player, false)));
		}
	}
}
