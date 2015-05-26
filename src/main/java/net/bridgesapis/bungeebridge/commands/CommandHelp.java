package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

public class CommandHelp extends Command {

    private HashSet<String> helps = new HashSet<>();

    public CommandHelp() {
        super("help");

        if (BungeeBridge.getInstance().getPrivateMessagesManager() != null)
            helps.add("msg");

        if (BungeeBridge.getInstance().hasLobbySwitcher())
            helps.add("lobby");

        if (BungeeBridge.getInstance().hasFriends())
            helps.add("friends");
    }

    @Override
    public void execute(CommandSender cs, String[] strings) {
        TextComponent help = new TextComponent(StringUtils.capitalize(I18n.getTranslation("words.help")) + " " + I18n.getTag());
        help.setBold(true);
        help.setColor(ChatColor.GOLD);

        TextComponent tiret = new TextComponent("-----");
        tiret.setColor(ChatColor.GREEN);

        TextComponent head = new TextComponent(tiret);
        head.addExtra(help);
        head.addExtra(tiret);

        cs.sendMessage(head);
        cs.sendMessage(new ComponentBuilder(I18n.getTranslation("commands.help.available")).color(ChatColor.YELLOW).bold(true).create());

        for (String com : helps) {
            TextComponent command = new TextComponent(I18n.getTranslation("help." + com + ".usage"));
            command.setColor(ChatColor.GREEN);

            TextComponent args = new TextComponent(" : "+I18n.getTranslation("help." + com + ".help"));
            args.setColor(ChatColor.GOLD);

            TextComponent toShow = new TextComponent("- ");
            toShow.setColor(ChatColor.GREEN);
            toShow.addExtra(command);
            toShow.addExtra(args);
            cs.sendMessage(toShow);
        }
    }
}
