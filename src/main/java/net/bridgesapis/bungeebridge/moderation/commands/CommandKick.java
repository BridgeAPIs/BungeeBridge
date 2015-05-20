package net.bridgesapis.bungeebridge.moderation.commands;

import com.google.gson.Gson;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.moderation.JsonCaseLine;
import net.bridgesapis.bungeebridge.moderation.JsonModMessage;
import net.bridgesapis.bungeebridge.moderation.ModerationTools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.bridgesapis.bungeebridge.core.database.Publisher;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.UUID;

public class CommandKick extends ModCommand {
    public CommandKick(BungeeBridge pl) {
        super("kick", "modotools.kick", pl);
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new ComponentBuilder("Utilisation : /kick <joueur> <motif>").color(ChatColor.RED).create());
            return;
        }

        final String player = args[0];
        final String reason = (args.length > 1) ? StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ") : "Vous avez été kické.";
        final TextComponent component = new TextComponent(reason);
        component.setColor(ChatColor.RED);

        BungeeBridge.getInstance().getExecutor().addTask(() -> {
            UUID id = bridge.getUuidTranslator().getUUID(player, true);
            if (id == null) {
                sender.sendMessage(new ComponentBuilder("Le joueur recherché n'a pas été trouvé.").color(ChatColor.RED).create());
                return;
            }

            if (! ModerationTools.canSanction(id)) {
                sender.sendMessage(new ComponentBuilder("Il est impossible de sanctionner ce joueur (E_PERM_HIGHER).").color(ChatColor.RED).create());
                return;
            }

            sender.sendMessage(new ComponentBuilder("Le joueur a été kické.").color(ChatColor.GREEN).create());
            BungeeBridge.getInstance().getPublisher().publish(new Publisher.PendingMessage("apiexec.kick", id + " " + new Gson().toJson(component)));

            // Sanction
            JsonCaseLine line = new JsonCaseLine();
            line.setAddedBy(sender.getName());
            line.setMotif(reason);
            line.setType("Kick");
            line.setDurationTime(0L);

            ModerationTools.checkFile(id, player, sender);
            ModerationTools.addSanction(line, id);

            JsonModMessage.build(sender, "Joueur " + player + " kické pour le motif " + reason + ".").send();
        });
    }
}
