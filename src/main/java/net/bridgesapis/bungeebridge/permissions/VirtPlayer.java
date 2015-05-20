package net.bridgesapis.bungeebridge.permissions;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.crosspermissions.api.rawtypes.RawPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class VirtPlayer implements RawPlayer {

    private UUID playerId;
    private ProxiedPlayer player;

    public VirtPlayer(UUID id) {
        this.player = ProxyServer.getInstance().getPlayer(id);
        this.playerId = id;
    }

    @Override
    public void setPermission(String s, boolean b) {
        if (player != null)
            player.setPermission(s, b);
    }

    @Override
    public UUID getUniqueId() {
        return playerId;
    }

    @Override
    public void clearPermissions() {
        if (player != null) {
            ArrayList<String> perms = new ArrayList<>();
            for (String perm : player.getPermissions())
                perms.add(perm);

            for (String perm : perms)
                player.setPermission(perm, false); // Conflits ? Possible.
        }
    }
}
