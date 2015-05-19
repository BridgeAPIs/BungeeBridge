package net.zyuiop.bungeebridge.moderation.commands;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;
import net.zyuiop.bungeebridge.moderation.JsonCaseLine;
import net.zyuiop.bungeebridge.moderation.JsonModMessage;
import net.zyuiop.bungeebridge.moderation.ModerationTools;
import net.zyuiop.bungeebridge.utils.Misc;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.*;

public class CommandBan extends ModCommand {
    public CommandBan(BungeeBridge plugin) {
        super("ban", "modotools.ban", plugin);
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(new ComponentBuilder("Utilisation : /ban <joueur> <durée|(def/perm)> <motif>").color(ChatColor.RED).create());
            return;
        }

        final String player = args[0];
        final String duration = args[1];
        final String reason = (args.length > 2) ? StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ") : null;

        bridge.getExecutor().addTask(() -> {
            UUID id = bridge.getUuidTranslator().getUUID(player, true);
            if (id == null) {
                sender.sendMessage(new ComponentBuilder("Le joueur recherché n'a pas été trouvé.").color(ChatColor.RED).create());
                return;
            }

            if (! ModerationTools.canSanction(id)) {
                sender.sendMessage(new ComponentBuilder("Il est impossible de sanctionner ce joueur (E_PERM_HIGHER).").color(ChatColor.RED).create());
                return;
            }

            Jedis jedis = bridge.getConnector().getResource();
            jedis.set("banlist:reason:" + id, (reason == null) ? "Vous êtes banni." : reason);

            JsonCaseLine caseLine = new JsonCaseLine();
            caseLine.setAddedBy(sender.getName());
            caseLine.setMotif((reason == null) ? "Vous êtes banni." : reason);
            caseLine.setType("Bannissement");
            if (!duration.equalsIgnoreCase("perm") && !duration.equalsIgnoreCase("permanent") && !duration.equalsIgnoreCase("def")) {
                // Parse duration
                Date end = Misc.parseTime(args[1]);
                int time = (int) ((end.getTime() - new Date().getTime()) / 1000) + 1;
                jedis.expire("banlist:reason:" + id, time);
                caseLine.setDuration(Misc.formatTime(time));
                caseLine.setDurationTime((long) time);
            } else {
                caseLine.setDuration("Définitif");
                caseLine.setDurationTime(-1L);
                caseLine.setGrave();
            }

            jedis.close();

            TextComponent kickReason = new TextComponent((reason == null) ? "Vous êtes banni." : reason);
            kickReason.setColor(ChatColor.RED);
            BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("apiexec.kick", id + " " + new Gson().toJson(kickReason)));
            sender.sendMessage(new ComponentBuilder("Le joueur a été banni.").color(ChatColor.GREEN).create());
            JsonModMessage.build(sender, "Joueur " + player + " banni pour le motif " + reason + ". Durée : " + caseLine.getDuration()).send();

            ModerationTools.addSanction(caseLine, id);
            ModerationTools.checkFile(id, player, sender);
        });
    }
}
