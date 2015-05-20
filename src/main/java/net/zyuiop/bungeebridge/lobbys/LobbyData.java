package net.zyuiop.bungeebridge.lobbys;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.zyuiop.bungeebridge.BungeeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

// TODO : Rewrite this file as we don't own it

public class LobbyData {

    public static final int MAX_LOBBY_COUNT = 120;
    public ConcurrentLinkedQueue<LobbyServer> servers = new ConcurrentLinkedQueue<>();
    public BungeeBridge plugin;

    public LobbyData(BungeeBridge plugin) {
        this.plugin = plugin;

        ProxyServer.getInstance().getScheduler().schedule(plugin, this::fetch, 1000L, 500L, TimeUnit.MILLISECONDS);
        ProxyServer.getInstance().getScheduler().schedule(plugin, this::areUp, 5L, 5L, TimeUnit.SECONDS);
    }

    public void fetch() {
        try {
            for (String server : ProxyServer.getInstance().getServers().keySet()) {
                if (server == null || !server.startsWith("Lobby_")) {
                    continue;
                }

                List<UUID> nb = new ArrayList<>();
                try {
                    nb.addAll(plugin.getServersManager().getServer(server).getPlayers());
                } catch(NullPointerException e) {}

                LobbyServer.Level good = getGoodLevel(nb.size());

                LobbyServer srv = getServerByName(server);
                if (srv == null) {
                    srv = new LobbyServer(server, true, nb, MAX_LOBBY_COUNT, good);
                    servers.add(srv);
                } else {
                    srv.setConnectedPlayers(nb);
                    srv.setLevel(good);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void areUp() {
        for (final LobbyServer serv : servers) {
            final ServerInfo srv = ProxyServer.getInstance().getServerInfo(serv.getServerName());
			if (srv == null) {
				servers.remove(serv);
				return;
			}
        }
    }

    public LobbyServer getServerByName(String n) {
        for(LobbyServer s : servers) {
            if(s.getServerName().equals(n))
                return s;
        }
        return null;
    }

    public LobbyServer.Level getGoodLevel(int countPlayer)
    {
        return LobbyServer.Level.getGoodLevel((countPlayer/MAX_LOBBY_COUNT)*100);
    }
}
