package net.bridgesapis.bungeebridge.interactions.friends;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.utils.TeleportTools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class FriendsCommand extends Command implements TabExecutor {

    protected FriendsManagement friendsManagement;

    public FriendsCommand(FriendsManagement friendsManagement) {
        super("friends", null, "f", "friend");
        this.friendsManagement = friendsManagement;
    }

    public void showHelp(CommandSender target) {
        target.sendMessage(new TextComponent(ChatColor.GREEN+"----- " + ChatColor.GOLD + "Aide amis :"+ChatColor.GREEN+" -----"));
        target.sendMessage(new ComponentBuilder(ChatColor.GOLD+"- "+ChatColor.GREEN+"/friends add <pseudo>"+ChatColor.GOLD+" : envoie une demande d'ami au joueur").create()[0]);
        target.sendMessage(new ComponentBuilder(ChatColor.GOLD+"- "+ChatColor.GREEN+"/friends accept <pseudo>"+ChatColor.GOLD+" : accepte un joueur en ami").create()[0]);
        target.sendMessage(new ComponentBuilder(ChatColor.GOLD+"- "+ChatColor.GREEN+"/friends deny <pseudo>"+ChatColor.GOLD+" : refuse la demande d'ami du joueur").create()[0]);
        target.sendMessage(new ComponentBuilder(ChatColor.GOLD+"- "+ChatColor.GREEN+"/friends remove <pseudo>"+ChatColor.GOLD+" : supprime le joueur de vos amis").create()[0]);
        target.sendMessage(new ComponentBuilder(ChatColor.GOLD+"- "+ChatColor.GREEN+"/friends requests"+ChatColor.GOLD+" : affiche la liste de vos demandes d'ami").create()[0]);
        target.sendMessage(new ComponentBuilder(ChatColor.GOLD+"- "+ChatColor.GREEN+"/friends list"+ChatColor.GOLD+" : affiche la liste de vos amis").create()[0]);
        target.sendMessage(new ComponentBuilder(ChatColor.GOLD+"- "+ChatColor.GREEN+"/friends tp <pseudo>"+ChatColor.GOLD+" : vous téléporte sur le même serveur que votre ami").create()[0]);
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder("Vous n'êtes pas un joueur.").color(ChatColor.RED).create());
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) sender;

        BungeeBridge.getInstance().getExecutor().addTask(() -> {
                if (args.length == 0) {
                    showHelp(sender);
                } else {
                    String command = args[0];
                    String arg = null;
                    if (args.length > 1)
                        arg = args[1];

                    if (command.equalsIgnoreCase("help")) {
                        showHelp(sender);
                    } else if (command.equalsIgnoreCase("add")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder("Cette commande nécessite un argument.").color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
                            target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.sendRequest(player.getUniqueId(), target);
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("accept")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder("Cette commande nécessite un argument.").color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.grantRequest(target, player.getUniqueId());
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("deny")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder("Cette commande nécessite un argument.").color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.denyRequest(target, player.getUniqueId());
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("remove")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder("Cette commande nécessite un argument.").color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.removeFriend(player.getUniqueId(), target);
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("tp")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder("Cette commande nécessite un argument.").color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder("Le pseudo n'est pas valide.").color(ChatColor.RED).create());
                            return;
                        }

                        if (!friendsManagement.isFriend(player.getUniqueId(), target)) {
                            sender.sendMessage(new ComponentBuilder("Vous n'êtes pas ami avec cette personne.").color(ChatColor.RED).create());
                            return;
                        }

                        TeleportTools.teleportFriend(player, target);
                    } else if (command.equalsIgnoreCase("requests")) {
                        TextComponent text = new TextComponent(ChatColor.YELLOW+"----- "+ChatColor.GOLD+"Voici vos demandes d'ami en attente :"+ChatColor.YELLOW+" -----");
                        player.sendMessage(text);
                        for (String pseudo : friendsManagement.requestsList(player.getUniqueId())) {
                            TextComponent line = new TextComponent(ChatColor.GOLD+"-> Demande de "+ChatColor.AQUA+pseudo+ChatColor.GOLD+" : ");
                            TextComponent accept = new TextComponent("[Accepter]");
                            accept.setColor(ChatColor.GREEN);
                            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends accept "+pseudo));
                            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN+"Accepter la demande d'ami").create()));
                            TextComponent refuse = new TextComponent("[Refuser]");
                            refuse.setColor(ChatColor.RED);
                            refuse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends deny "+pseudo));
                            refuse.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Refuser la demande d'ami").create()));
                            line.addExtra(accept);
                            line.addExtra(new ComponentBuilder(" ou ").color(ChatColor.GOLD).create()[0]);
                            line.addExtra(refuse);
                            player.sendMessage(line);
                        }
                    } else if (command.equalsIgnoreCase("list")) {

                        HashMap<UUID, String> list = friendsManagement.associatedFriendsList(player.getUniqueId());

                        ArrayList<TextComponent> messages = new ArrayList<>();
                        for (Map.Entry<UUID, String> entry : list.entrySet()) {
                            String pseudo = entry.getValue();
                            TextComponent line = new TextComponent(ChatColor.GOLD+"-> ");
                            TextComponent tpseudo = new TextComponent(pseudo+ " ");
                            if (friendsManagement.plugin.getNetworkBridge().isOnline(entry.getKey()))
                                tpseudo.setColor(ChatColor.GREEN);
                            else
                                tpseudo.setColor(ChatColor.RED);
                            TextComponent refuse = new TextComponent("[Supprimer]");
                            refuse.setColor(ChatColor.RED);
                            refuse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends remove "+pseudo));
                            refuse.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED+"Supprimer cet ami").create()));
                            line.addExtra(tpseudo);
                            line.addExtra(refuse);
                            messages.add(line);
                        }

                        TextComponent text = new TextComponent(ChatColor.YELLOW+"----- "+ChatColor.GOLD+"Voici votre liste d'amis :"+ChatColor.YELLOW+" -----");
                        player.sendMessage(text);

                        for (TextComponent t : messages)
                            player.sendMessage(t);
                    } else {
                        sender.sendMessage(new ComponentBuilder("Cette commande n'est pas reconnue.").color(ChatColor.RED).create());
                    }
                }

        });


    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        List<String> result = new ArrayList<>();

        if (args.length < 2) {
           return result;
        } else {
			result.addAll(BungeeBridge.getInstance().getNetworkBridge().getOnlinePlayersNames().stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList()));

			return result;
        }
    }
}
