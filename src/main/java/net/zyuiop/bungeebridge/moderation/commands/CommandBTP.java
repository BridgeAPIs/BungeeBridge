package net.zyuiop.bungeebridge.moderation.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.core.players.PlayerData;
import net.zyuiop.bungeebridge.core.database.Publisher;

import java.util.UUID;

public class CommandBTP extends ModCommand {
    public CommandBTP(BungeeBridge bridge) {
        super("btp", "modoutils.tp", bridge);
    }

    @Override
    public void execute(final CommandSender arg0, final String[] arg1) {
        bridge.getExecutor().addTask(() -> {
            ProxiedPlayer p = (ProxiedPlayer) arg0;

            // Vérification des arguments
            if (arg1.length < 1) {
                TextComponent message = new TextComponent("Syntaxe : /btp <pseudo>");
                message.setColor(ChatColor.RED);
                arg0.sendMessage(message);
                return;
            }

            // Récupération de l'UUID
            UUID id = bridge.getUuidTranslator().getUUID(arg1[0], true);

            if (id == null) {
                TextComponent message = new TextComponent("Le joueur recherché n'existe pas.");
                message.setColor(ChatColor.RED);
                arg0.sendMessage(message);
                return;
            }

            if (!bridge.getNetworkBridge().isOnline(id)) {
                TextComponent message = new TextComponent("Le joueur recherché n'est pas en ligne.");
                message.setColor(ChatColor.RED);
                arg0.sendMessage(message);
                return;
            }

            PlayerData data = bridge.getPlayerDataManager().getPlayerData(id);
            String server = data.get("currentserver");

            if (server == null) {
                TextComponent message = new TextComponent("Le joueur recherché n'est pas en ligne.");
                message.setColor(ChatColor.RED);
                arg0.sendMessage(message);
            } else {
                ServerInfo info = ProxyServer.getInstance().getServerInfo(server);
                if (info == null) {
                    TextComponent message = new TextComponent("Le joueur recherché n'est pas en ligne.");
                    message.setColor(ChatColor.RED);
                    arg0.sendMessage(message);
                } else {
                    bridge.getPublisher().publish(new Publisher.PendingMessage(server, "teleport " + p.getUniqueId() + " " + id));
                    bridge.getPublisher().publish(new Publisher.PendingMessage(server, "moderator " + p.getUniqueId()));
                }
            }
        });
    }
}
