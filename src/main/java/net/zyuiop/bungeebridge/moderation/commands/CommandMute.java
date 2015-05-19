package net.zyuiop.bungeebridge.moderation.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.database.Publisher;
import net.zyuiop.bungeebridge.moderation.JsonCaseLine;
import net.zyuiop.bungeebridge.moderation.JsonModMessage;
import net.zyuiop.bungeebridge.moderation.ModerationTools;
import net.zyuiop.bungeebridge.utils.Misc;
import net.samagames.permissionsapi.permissions.PermissionUser;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.*;

public class CommandMute extends ModCommand {

    public CommandMute(BungeeBridge bridge) {
        super("mute", "modo.mute", bridge);
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new ComponentBuilder("Utilisation : /mute <joueur> <durée> <motif>").color(ChatColor.RED).create());
            sender.sendMessage(new ComponentBuilder("Unmute : /mute <joueur>").color(ChatColor.GREEN).create());
            return;
        }

        final String player = args[0];
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
                String data = jedis.get("mute:" + id);
                if (data != null && args.length == 1) {
					jedis.del("mute:" + id);
                    jedis.close();
                    bridge.getPublisher().publish(new Publisher.PendingMessage("mute.remove", "" + id));
                    sender.sendMessage(new ComponentBuilder("Le joueur a bien été unmute.").color(ChatColor.GREEN).create());
                    return;
                }
                jedis.close();


                if (args.length < 2) {
                    sender.sendMessage(new ComponentBuilder("Utilisation : /mute <joueur> <durée> <motif>").color(ChatColor.RED).create());
                    sender.sendMessage(new ComponentBuilder("Unmute : /mute <joueur>").color(ChatColor.GREEN).create());
                    return;
                }

                Date end = Misc.parseTime(args[1]);
                long time = (end.getTime() - new Date().getTime()) / 1000;
                PermissionUser user = (sender instanceof ProxiedPlayer) ? bridge.getPermissionsBridge().getApi().getUser(((ProxiedPlayer) sender).getUniqueId()) : null;
                if (time > 600 && !(user != null && user.hasPermission("mute.longtime"))) {
                    sender.sendMessage(new ComponentBuilder("Votre groupe ne vous autorise pas a mute un joueur plus de 10 minutes.").color(ChatColor.RED).create());
                    return;
                }

				jedis = bridge.getConnector().getResource();
                jedis.set("mute:" + id, end.getTime() + "");
                jedis.expireAt("mute:" + id, end.getTime() / 1000);
                String motif = (args.length > 2) ? StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ") : "Vous êtes muet.";

                jedis.set("mute:" + id + ":reason", motif);
                jedis.expireAt("mute:" + id + ":reason", end.getTime() / 1000);
                jedis.close();

                bridge.getPublisher().publish(new Publisher.PendingMessage("mute.add", id + " " + end.getTime() + " " + motif));

                String f_time = Misc.formatTime(((end.getTime() - System.currentTimeMillis())/1000) + 1);
                TextComponent duration = new TextComponent(f_time);
                duration.setColor(ChatColor.AQUA);
                TextComponent message = new TextComponent("Le joueur est maintenant muet pour une durée de ");
                message.setColor(ChatColor.GREEN);
                message.addExtra(duration);

                sender.sendMessage(message);

                String fromPseudo = ((sender instanceof ProxiedPlayer) ? ((ProxiedPlayer) sender).getDisplayName() : "CONSOLE");
                JsonCaseLine sanction = new JsonCaseLine();
                sanction.setAddedBy(fromPseudo);
                sanction.setDuration(f_time);
                sanction.setMotif(motif);
                sanction.setType("Mute");
				sanction.setDurationTime((end.getTime() - System.currentTimeMillis()) / 1000);
				if (time > 3600 * 24 * 45)
					sanction.setGrave();

				ModerationTools.checkFile(id, player, sender);
				ModerationTools.addSanction(sanction, id);
                JsonModMessage.build(sender, "Joueur " + player + " muet pour le motif " + motif + ". Durée : " + sanction.getDuration()).send();
        });
    }
}
