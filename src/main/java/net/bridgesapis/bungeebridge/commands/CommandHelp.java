package net.bridgesapis.bungeebridge.commands;

import net.bridgesapis.bungeebridge.i18n.I18n;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class CommandHelp extends Command {

    private static HashMap<String, String> help  = new HashMap<>();

    public CommandHelp() {
        super("help");
    }

    public static void addHelp(String command, String helpMessage) {
        help.put(command, helpMessage);
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

        for (Map.Entry<String, String> entry : CommandHelp.help.entrySet()) {
            TextComponent command = new TextComponent(entry.getKey());
            command.setColor(ChatColor.GREEN);

            TextComponent args = new TextComponent(" : "+ entry.getValue());
            args.setColor(ChatColor.GOLD);

            TextComponent toShow = new TextComponent("- ");
            toShow.setColor(ChatColor.GREEN);
            toShow.addExtra(command);
            toShow.addExtra(args);
            cs.sendMessage(toShow);
        }
    }
}
