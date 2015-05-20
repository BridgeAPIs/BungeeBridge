package net.bridgesapis.bungeebridge.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.bridgesapis.bungeebridge.BungeeBridge;

import java.util.UUID;

public class TeleportTools {

    public static ServerInfo getPlayerServer(UUID player) {
        if (ProxyServer.getInstance().getPlayer(player) != null)
            return ProxyServer.getInstance().getPlayer(player).getServer().getInfo();

        if (!BungeeBridge.getInstance().getNetworkBridge().isOnline(player))
            return null;

        return BungeeBridge.getInstance().getServersManager().getServer(player);
    }

    public static ServerInfo getPlayerServer(String playerName) {
        if (ProxyServer.getInstance().getPlayer(playerName) != null)
            return ProxyServer.getInstance().getPlayer(playerName).getServer().getInfo();

        UUID playerId = BungeeBridge.getInstance().getUuidTranslator().getUUID(playerName, false);

        return getPlayerServer(playerId);
    }

    public static void teleportMod(final ProxiedPlayer teleported, ServerInfo server) {
        // TODO : Rewrite function
    }


    /**
     * Téléporte un joueur vers un autre
     * @param teleported Joueur a téléporter
     * @param teleportTo Joueur cible
     * @param serverOnly falsee : téléporter a la position exacte / true : téléporter sur le même serveur
     * @return
     */
    public static void teleportTo(final ProxiedPlayer teleported, final UUID teleportTo, final boolean serverOnly) {
        final ServerInfo server = getPlayerServer(teleportTo);
        if (server == null) {
            return;
        }

        String send = null;
		// TODO : Rewrite
    }

    public static void teleportFriend(final ProxiedPlayer teleported, final UUID targetID) {
        // 1. Est il en ligne ? A-t-il un UUID ?
        final ServerInfo server = getPlayerServer(targetID);
        if (server == null) {
            teleported.sendMessage(new ComponentBuilder("Le joueur n'est pas en ligne.").color(ChatColor.RED).create());
            return;
        }

        /*UnknownPlayer upl = new UnknownPlayer(teleported.getUniqueId(), teleported.getName());
        Party party = ModoUtils.instance.partiesManager.getParty(upl);
        if (party != null && !server.getName().startsWith("Lobby_")) {
            if (party.isLeader(upl)) {
                new PartyConnect(party.getPartyId(), server.getName()).sendPacket();
            } else {
                teleported.sendMessage(new ComponentBuilder("Seul le chef de partie peut rejoindre un jeu.").color(ChatColor.RED).create());
            }
            return;
        }*/

        teleported.connect(server);
    }

    public enum TeleportState {

        OFFLINE("Le joueur recherché est actuellement hors ligne.", true),
        OKAY("Vous avez bien été téléporté !", false),
        REFUSED("Une erreur s'est produite.", true);

        private String message;
        private boolean failed;
        private TeleportState(String message, boolean failed) {
            this.message = message;
            this.failed = failed;
        }

        public void showMessage(ProxiedPlayer p) {
            TextComponent msg = new TextComponent(message);
            if (failed)
                msg.setColor(ChatColor.RED);
            else
                msg.setColor(ChatColor.GREEN);
            p.sendMessage(msg);
        }
    }
}
