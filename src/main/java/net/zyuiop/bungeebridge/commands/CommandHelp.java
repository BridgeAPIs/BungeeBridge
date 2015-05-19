package net.zyuiop.bungeebridge.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;

public class CommandHelp extends Command {

    private HashMap<String, String> helps = new HashMap<>();
    private HashMap<String, String> lobHelp = new HashMap<>();

    public CommandHelp() {
        super("help");

        helps.put("/msg <joueur> <message>", "Permet d'envoyer un message privé à un joueur");
        helps.put("/lobby [numéro lobby]", "Vous téléporte au lobby");
        helps.put("/report <joueur> <message>", "Permet de reporter un joueur au staff");
        helps.put("/coins", "Affiche votre nombre de coins");
        helps.put("/friends help", "Aide des commandes d'amis");

        lobHelp.put("/click <joueur>", "Affiche le clickme d'un joueur");
		lobHelp.put("/woot", "Marque votre appréciation à la musique actuellement jouée.");
		lobHelp.put("/meh", "Marque que la musique actuelle ne vous plait pas.");
		lobHelp.put("/playlist", "Affiche la file d'attente de musiques sur le lobby.");
    }

    @Override
    public void execute(CommandSender cs, String[] strings) {
        // # - AIDE DU LOBBY - # //
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

        cs.sendMessage(new ComponentBuilder(" ").create());
        cs.sendMessage(new ComponentBuilder("Commandes pour les lobbys uniquement :").color(ChatColor.YELLOW).bold(true).create());
        for (String com : lobHelp.keySet()) {
            TextComponent command = new TextComponent(com);
            command.setColor(ChatColor.GREEN);

            TextComponent args = new TextComponent(" : "+lobHelp.get(com));
            args.setColor(ChatColor.GOLD);

            TextComponent toShow = new TextComponent("- ");
            toShow.setColor(ChatColor.GREEN);
            toShow.addExtra(command);
            toShow.addExtra(args);
            cs.sendMessage(toShow);
        }

        cs.sendMessage(new ComponentBuilder(" ").create());
        cs.sendMessage(new ComponentBuilder("Informations supplémentaires :").color(ChatColor.YELLOW).bold(true).create());
        cs.sendMessage(new ComponentBuilder("Retrouvez nous sur TeamSpeak :").color(ChatColor.GOLD).create());
        cs.sendMessage(new ComponentBuilder("ts.samagames.net").color(ChatColor.AQUA).italic(true).create());
        cs.sendMessage(new ComponentBuilder("Boutique & site internet :").color(ChatColor.GOLD).create());
        cs.sendMessage(new ComponentBuilder("http://www.samagames.net/").color(ChatColor.AQUA).italic(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.samagames.net/")).create());
        cs.sendMessage(head);
    }
}
