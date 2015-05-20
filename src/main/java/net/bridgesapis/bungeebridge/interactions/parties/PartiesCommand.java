package net.bridgesapis.bungeebridge.interactions.parties;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.commands.DefaultExecutor;
import net.bridgesapis.bungeebridge.core.database.Publisher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.util.*;

public class PartiesCommand extends DefaultExecutor {

	// TODO : WRITE HELP

	private final PartiesManager partiesManager;
	private final BungeeBridge bungeeBridge;

	public PartiesCommand(PartiesManager partiesManager, BungeeBridge bungeeBridge) {
		super("party", null, "parties", "p");
		this.partiesManager = partiesManager;
		this.bungeeBridge = bungeeBridge;

		if (bungeeBridge.hasFriends())
			helps.put("friends", "invite tous vos amis dans une partie");
		helps.put("invite <pseudo>", "invite un joueur dans votre partie");
		helps.put("accept [pseudo]", "accepte l'invitation d'un joueur");
		helps.put("refuse [pseudo]", "refuse l'invitation d'un joueur");
		helps.put("lead <pseudo>", "nomme un joueur chef de votre partie");
		helps.put("kick <pseudo>", "expulse un joueur de votre partie");
		//helps.put("tp", "vous téléporte aux autres membres de la partie");
		//helps.put("tpall", "téléporte tous les joueurs de la partie sur votre serveur");
		helps.put("list", "liste les joueurs dans la partie");
		helps.put("leave", "vous sort de la partie");
		helps.put("disband", "supprime la partie");
	}

	protected HashMap<String, String> helps = new HashMap<>();

	public void showHelp(CommandSender target) {
		target.sendMessage(new TextComponent(ChatColor.GREEN+"----- " + ChatColor.GOLD + "Aide parties :"+ChatColor.GREEN+" -----"));
		for (Map.Entry<String, String> helpline :  helps.entrySet()) {
			TextComponent component = new TextComponent("- ");
			component.setColor(ChatColor.GOLD);
			TextComponent command = new TextComponent("/party "+helpline.getKey());
			command.setColor(ChatColor.GREEN);
			TextComponent help = new TextComponent(" : " + helpline.getValue());
			help.setColor(ChatColor.GOLD);
			component.addExtra(command);
			component.addExtra(help);
			target.sendMessage(component);
		}
	}

	@Override
	public void execute(CommandSender sender, String[] strings) {
		if (strings.length < 1) {
			showHelp(sender);
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		String command = strings[0];
		String[] args;
		if (strings.length > 1)
			args = Arrays.copyOfRange(strings, 1, strings.length);
		else
			args = new String[0];

		if (command.equalsIgnoreCase("accept")) {
			if (args.length >= 1) {
				partiesManager.accept(player, args[0]);
			} else {
				partiesManager.accept(player);
			}
		} else if (command.equalsIgnoreCase("list")) {
			UUID party = getPartyId(player);
			if (party == null)
				return;

			TextComponent msg = new TextComponent("Voici les membres de la party :");
			msg.setColor(ChatColor.GREEN);
			UUID leader = partiesManager.getLeader(party);
			for (Map.Entry<UUID, String> entry : partiesManager.getPlayersInParty(party).entrySet()) {
				TextComponent pl = new TextComponent(entry.getValue() + (leader.equals(entry.getKey()) ? "*" : ""));
				if (BungeeBridge.getInstance().getNetworkBridge().isOnline(entry.getKey())) {
					pl.setColor(ChatColor.GREEN);
				} else {
					pl.setColor(ChatColor.RED);
				}

				msg.addExtra(" ");
				msg.addExtra(pl);
			}

			player.sendMessage(msg);
		} else if (command.equalsIgnoreCase("invite")) {
			if (args.length < 1) {
				error("Veuillez indiquer le pseudo du joueur à inviter.", sender);
				return;
			}

			String name = args[0];
			UUID id = bungeeBridge.getUuidTranslator().getUUID(name, false);
			if (id == null) {
				error("Le joueur n'a pas été trouvé.", sender);
			} else if (partiesManager.getPlayerParty(id) != null) {
				error("Le joueur est déjà en party.", sender);
			} else if (!bungeeBridge.getNetworkBridge().isOnline(id)) {
				error("Le joueur n'est pas connecté.", sender);
			} else {
				bungeeBridge.getPublisher().publish(new Publisher.PendingMessage("parties.invite", ((ProxiedPlayer) sender).getUniqueId() + " " + sender.getName() + " " + id, () -> sender.sendMessage(new ComponentBuilder("L'invitation a bien été envoyée.").color(ChatColor.GREEN).create())));
			}
 		} else if (command.equalsIgnoreCase("friends") && bungeeBridge.hasFriends()) {
			bungeeBridge.getFriendsManagement().UUIDFriendList(player.getUniqueId()).stream().filter(id -> bungeeBridge.getNetworkBridge().isOnline(id)).forEach(id -> bungeeBridge.getPublisher().publish(new Publisher.PendingMessage("parties.invite", ((ProxiedPlayer) sender).getUniqueId() + " " + sender.getName() + " " + id, () -> sender.sendMessage(new ComponentBuilder("L'invitation a bien été envoyée.").color(ChatColor.GREEN).create()))));
			player.sendMessage(new ComponentBuilder("Les demandes ont été envoyées.").color(ChatColor.GREEN).create());
		} else if (command.equalsIgnoreCase("lead")) {
			UUID party = getPartyId(player);
			if (party == null)
				return;

			if (isLeader(party, player)) {
				if (args.length < 1) {
					error("Veuillez indiquer le pseudo du joueur à nommer chef.", sender);
					return;
				}

				String name = args[0];
				UUID id = bungeeBridge.getUuidTranslator().getUUID(name, false);
				if (id == null) {
					error("Le joueur n'a pas été trouvé.", sender);
				} else if (!partiesManager.getPlayersInParty(party).containsKey(id)) {
					error("Le joueur n'est pas dans la party.", sender);
				} else {
					partiesManager.setLeader(party, id);
					bungeeBridge.getPublisher().publish(new Publisher.PendingMessage("parties.lead", party + " " + name));
				}
			}
		} else if (command.equalsIgnoreCase("disband")) {
			UUID party = getPartyId(player);
			if (party == null)
				return;

			if (isLeader(party, player)) {
				Jedis jedis = bungeeBridge.getConnector().getBungeeResource();
				jedis.del("party:" + party + ":server");
				jedis.del("party:" + party + ":lead");
				Set<String> members = jedis.hgetAll("party:" + party + ":members").keySet();
				jedis.del("party:" + party + ":members");
				members.stream().forEach(k -> {
					jedis.del("currentparty:" + k);
					bungeeBridge.getPublisher().publish(new Publisher.PendingMessage("parties.disband", k));
				});
				jedis.close();
				player.sendMessage(new ComponentBuilder("La partie a bien été supprimée.").color(ChatColor.GREEN).create());
			}
		} else if (command.equalsIgnoreCase("leave")) {
			UUID party = getPartyId(player);
			if (party == null)
				return;

			partiesManager.leave(player.getUniqueId(), party);
		} else if (command.equalsIgnoreCase("kick")) {
			UUID party = getPartyId(player);
			if (party == null)
				return;

			if (isLeader(party, player)) {
				if (args.length < 1) {
					error("Veuillez indiquer le pseudo du joueur à exclure.", sender);
					return;
				}

				String name = args[0];
				UUID id = bungeeBridge.getUuidTranslator().getUUID(name, false);
				if (id == null) {
					error("Le joueur n'a pas été trouvé.", sender);
				} else if (!partiesManager.getPlayersInParty(party).containsKey(id)) {
					error("Le joueur n'est pas dans la party.", sender);
				} else {
					partiesManager.kick(id, party);
				}
			}
		}
	}

	UUID getPartyId(ProxiedPlayer player) {
		UUID party = partiesManager.getPlayerParty(player.getUniqueId());
		if (party == null) {
			error("Vous n'êtes pas dans une party.", player);
			return null;
		}
		return party;
	}

	boolean isLeader(UUID party, ProxiedPlayer player) {
		if (player.getUniqueId().equals(partiesManager.getLeader(party)))
			return true;

		error("Vous n'êtes pas le leader de la party.", player);
		return false;
	}

	void error(String text, CommandSender to) {
		TextComponent component = new TextComponent(text);
		component.setColor(ChatColor.RED);
		to.sendMessage(component);
	}
}
