package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;

public class CommandHelp extends Command {

    private HashMap<String, String> helps = new HashMap<>();

    public CommandHelp() {
        super("help");

        if (BungeeBridge.getInstance().getPrivateMessagesManager() != null)
            helps.put("/msg <joueur> <message>", "Permet d'envoyer un message privé à un joueur");

        if (BungeeBridge.getInstance().hasLobbySwitcher())
            helps.put("/lobby [numéro lobby]", "Vous téléporte au lobby");

        if (BungeeBridge.getInstance().hasFriends())
            helps.put("/friends help", "Aide des commandes d'amis");
    }

    @Override
    public void execute(CommandSender cs, String[] strings) {
        TextComponent help = new TextComponent("Aide BungeeBridge");
        help.setBold(true);
        help.setColor(ChatColor.GOLD);

        TextComponent tiret = new TextComponent("-----");
        tiret.setColor(ChatColor.GREEN);

        TextComponent head = new TextComponent(tiret);
        head.addExtra(help);
        head.addExtra(tiret);

        cs.sendMessage(head);
        cs.sendMessage(new ComponentBuilder("Voici les commandes disponibles sur le serveur :").color(ChatColor.YELLOW).bold(true).create());

        for (String com : helps.keySet()) {
            TextComponent command = new TextComponent(com);
            command.setColor(ChatColor.GREEN);

            TextComponent args = new TextComponent(" : "+helps.get(com));
            args.setColor(ChatColor.GOLD);

            TextComponent toShow = new TextComponent("- ");
            toShow.setColor(ChatColor.GREEN);
            toShow.addExtra(command);
            toShow.addExtra(args);
            cs.sendMessage(toShow);
        }
    }
}
