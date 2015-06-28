package net.bridgesapis.bungeebridge.interactions.friends;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.i18n.I18n;
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
        target.sendMessage(new TextComponent(I18n.getModuleTranslation("friends", "command.help.head")));
        target.sendMessage(new ComponentBuilder(getCommandHelp("add", "%NAME%")).create());
        target.sendMessage(new ComponentBuilder(getCommandHelp("accept", "%NAME%")).create());
        target.sendMessage(new ComponentBuilder(getCommandHelp("deny", "%NAME%")).create());
        target.sendMessage(new ComponentBuilder(getCommandHelp("remove", "%NAME%")).create());
        target.sendMessage(new ComponentBuilder(getCommandHelp("tp", "%NAME%")).create());
        target.sendMessage(new ComponentBuilder(getCommandHelp("requests", "")).create());
        target.sendMessage(new ComponentBuilder(getCommandHelp("list", "")).create());
    }

    private String getCommandHelp(String command, String args) {
        String global = I18n.getModuleTranslation("friends", "command.help.global_formating");
        String name = I18n.getModuleTranslation("friends", "command.help.name_argument_tag");
        String description = I18n.getModuleTranslation("friends", "command.help.helps." + command);

        args = command + (args.length() > 0 ? " " + args : "");
        args = args.replace("%NAME%", name);
        return global.replace("%ARGS%", args).replace("%DESCRIPTION%", description);
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.misc.not_a_player")).color(ChatColor.RED).create());
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
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.misc.missing_args")).color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
                            target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.sendRequest(player.getUniqueId(), target);
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("accept")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.grantRequest(target, player.getUniqueId());
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("deny")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.misc.missing_args")).color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.denyRequest(target, player.getUniqueId());
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("remove")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.misc.missing_args")).color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        String rep = friendsManagement.removeFriend(player.getUniqueId(), target);
                        sender.sendMessage(rep);
                    } else if (command.equalsIgnoreCase("tp")) {
                        if (arg == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.misc.missing_args")).color(ChatColor.RED).create());
                            return;
                        }

                        UUID target;
                        try {
							target = friendsManagement.plugin.getUuidTranslator().getUUID(arg, false);
                        } catch (Exception e) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        if (target == null) {
                            sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.locate.invalid")).color(ChatColor.RED).create());
                            return;
                        }

                        if (!friendsManagement.isFriend(player.getUniqueId(), target)) {
                            sender.sendMessage(new ComponentBuilder(I18n.getModuleTranslation("friends", "not_friend_with")).color(ChatColor.RED).create());
                            return;
                        }

                        TeleportTools.teleportFriend(player, target);
                    } else if (command.equalsIgnoreCase("requests")) {
                        TextComponent text = new TextComponent(I18n.getModuleTranslation("friends", "command.requests.head"));
                        player.sendMessage(text);
                        for (String pseudo : friendsManagement.requestsList(player.getUniqueId())) {
                            TextComponent line = new TextComponent(I18n.getModuleTranslation("friends", "command.requests.demand_from").replace("%NAME%", pseudo));
                            TextComponent accept = new TextComponent(I18n.getModuleTranslation("friends", "command.requests.demand_accept_button"));
                            accept.setColor(ChatColor.GREEN);
                            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends accept "+pseudo));
                            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(I18n.getModuleTranslation("friends", "command.requests.demand_accept_hover")).create()));
                            TextComponent refuse = new TextComponent(I18n.getModuleTranslation("friends", "command.requests.demand_refuse_button"));
                            refuse.setColor(ChatColor.RED);
                            refuse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends deny "+pseudo));
                            refuse.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(I18n.getModuleTranslation("friends", "command.requests.demand_refuse_hover")).create()));
                            line.addExtra(accept);
                            line.addExtra(new ComponentBuilder(" " + I18n.getWord("or") + " ").color(ChatColor.GOLD).create()[0]);
                            line.addExtra(refuse);
                            player.sendMessage(line);
                        }
                    } else if (command.equalsIgnoreCase("list")) {

                        HashMap<UUID, String> list = friendsManagement.associatedFriendsList(player.getUniqueId());

                        ArrayList<TextComponent> messages = new ArrayList<>();
                        for (Map.Entry<UUID, String> entry : list.entrySet()) {
                            String pseudo = entry.getValue();

                            boolean isOnline = friendsManagement.plugin.getNetworkBridge().isOnline(entry.getKey());
                            String name = (isOnline ? ChatColor.GREEN : ChatColor.RED) + pseudo;

                            TextComponent line = new TextComponent(I18n.getModuleTranslation("friends", "command.list.item_format").replace("%NAME%", name));

                            TextComponent refuse = new TextComponent("[Supprimer]");
                            refuse.setColor(ChatColor.RED);
                            refuse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends remove "+pseudo));
                            refuse.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED+"Supprimer cet ami").create()));
                            line.addExtra(refuse);
                            messages.add(line);
                        }

                        TextComponent text = new TextComponent(I18n.getModuleTranslation("friends", "command.list.head"));
                        player.sendMessage(text);

                        for (TextComponent t : messages)
                            player.sendMessage(t);
                    } else {
                        sender.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.misc.unknown_subcommand")).color(ChatColor.RED).create());
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
