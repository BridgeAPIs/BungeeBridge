package net.zyuiop.bungeebridge.moderation.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.moderation.JsonModMessage;
import redis.clients.jedis.Jedis;
import java.util.UUID;

public class CommandPardon extends ModCommand {
    public CommandPardon(BungeeBridge bridge) {
        super("pardon", "modotools.pardon", bridge, "unban");
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new ComponentBuilder("Utilisation : /pardon <joueur>").color(ChatColor.RED).create());
            return;
        }

        final String player = args[0];
        BungeeBridge.getInstance().getExecutor().addTask(() -> {
			UUID id = BungeeBridge.getInstance().getUuidTranslator().getUUID(player, true);
			if (id == null) {
				sender.sendMessage(new ComponentBuilder("Le joueur recherché n'a pas été trouvé.").color(ChatColor.RED).create());
				return;
			}

			Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
			String ban = jedis.get("banlist:reason:" + id);

			if (ban != null) {
				jedis.del("banlist:reason:" + id);
				sender.sendMessage(new ComponentBuilder("Le joueur a été débanni.").color(ChatColor.GREEN).create());
				JsonModMessage.build(sender, "Joueur " + player + " débanni.");
			} else {
				sender.sendMessage(new ComponentBuilder("Le joueur n'était pas banni.").color(ChatColor.RED).create());
			}

            jedis.close();
		});
    }
}
