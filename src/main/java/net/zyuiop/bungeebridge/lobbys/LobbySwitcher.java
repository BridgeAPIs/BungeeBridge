package net.zyuiop.bungeebridge.lobbys;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Geekpower14 on 21/12/2014.
 */

// TODO : Rewrite this file as we don't own it
public class LobbySwitcher {

    public BungeeBridge plugin;
    public LobbyData lobbyData;
    public ConcurrentHashMap<UUID, String> affected_Lobbys = new ConcurrentHashMap<>();
    private Random random;

    public LobbySwitcher(BungeeBridge plugin)
    {
        this.plugin = plugin;
        this.lobbyData = new LobbyData(plugin);
        random = new Random();
    }

    public void serverSelector(ProxiedPlayer p) {
        serverSelector(p, null, null);
    }
    public void serverSelector(ProxiedPlayer p, String exclude) {
        serverSelector(p, exclude, null);
    }

    public void serverSelector(final ProxiedPlayer p, final String exclude, final TextComponent lastKickReason)
    {
        affected_Lobbys.remove(p.getUniqueId());
        ProxyServer.getInstance().getLogger().info("LobbyJoiner : joining player " + p.getName() + " - " + p.getUniqueId());

        HashMap<LobbyServer, Integer> data = new HashMap<>();
		lobbyData.servers.stream().filter(srv -> (exclude == null || ! exclude.equals(srv.getServerName())) && srv.isOnline()).forEach(srv -> data.put(srv, 0));

        for(LobbyServer srv : data.keySet())
        {

            if(srv.getLevel().equals(LobbyServer.Level.EMERGENCY)) {
                data.put(srv, 0);
                continue;
            }

            ProxyServer.getInstance().getLogger().info("LobbyJoiner : calcul server" + srv.getServerName() + " - " + p.getUniqueId());

            int note = 0;

            if(srv.getLevel().equals(LobbyServer.Level.LOW)) {
                note += 3;
            } else if(srv.getLevel().equals(LobbyServer.Level.MEDIUM)) {
                note += 5;
            } else if(srv.getLevel().equals(LobbyServer.Level.CHARGED)) {
                note += 4;
            } else if(srv.getLevel().equals(LobbyServer.Level.FULL))  {
                note += 1;
            }

            data.put(srv, note);
        }

        TreeSet<Map.Entry<LobbyServer, Integer>> tree = new TreeSet<>((o1, o2) -> {
			return o2.getValue().compareTo(o1.getValue());
		});
        tree.addAll(data.entrySet());

        if (tree.size() < 1 || (tree.first().getValue() == 0))
        {
			p.disconnect((lastKickReason != null) ? lastKickReason : new ComponentBuilder("Tous les lobbys sont actuellement pleins.").color(ChatColor.RED).create()[0]);
            return;
        }

        LobbyServer server = tree.first().getKey();
        ProxyServer.getInstance().getLogger().info("[LobbyJoiner] Lobby choosed: " + server.getServerName() + " - For:" + p.getUniqueId());

        if(server.isOnline() && (server.getLevel() != LobbyServer.Level.EMERGENCY)) {
            ProxyServer.getInstance().getLogger().info("[" + server.getServerName() + "] Lobby found, save it for: "+ p.getName());
            saveServer(p.getUniqueId(), server.getServerName());
            return;
        }



        return;
    }

    public void saveServer(final UUID p, String server)
    {
        affected_Lobbys.put(p, server);
    }

    public String cursiveJoin(final ProxiedPlayer p) {
        if(affected_Lobbys.containsKey(p.getUniqueId())) {
            LobbyServer server = lobbyData.getServerByName(affected_Lobbys.get(p.getUniqueId()));
            if(server != null && server.isOnline() && (server.getLevel() != LobbyServer.Level.EMERGENCY) && canJoinServer(p, server)) {
                ProxyServer.getInstance().getLogger().info("Lobby found, connecting to it. [" + server.getServerName() + "]");
                return server.getServerName();
            }
        }
        serverSelector(p);

        return cursiveJoin(p);
    }

    public void kickFromLobby(final ProxiedPlayer p, String kickedFrom) {
        /*LobbyServer ls = lobbyData.getServerByName(kickedFrom);
        if(ls != null)
        {
            ls.setOnline(false);
        }*/
        serverSelector(p, kickedFrom);
    }

    public void changeLobby(final ProxiedPlayer p, String newLobby) {
        affected_Lobbys.put(p.getUniqueId(), newLobby);
    }

    public boolean canJoinServer(final ProxiedPlayer p, final LobbyServer server)
    {
        PermissionsAPI api = plugin.getPermissionsBridge().getApi();
        PermissionUser user = api.getUser(p.getUniqueId());
        if(server.getServerName().startsWith("Lobby_") && (server.getConnectedPlayers().size() > LobbyData.MAX_LOBBY_COUNT))
        {
            if(user.hasPermission("netjoin.full"))
            {
                return true;
            }
            return false;
        }

        return true;
    }

    public void researchLobby(ProxiedPlayer p)
    {
        //On cherche un lobby dispo
        serverSelector(p);
        //On le connecte
        cursiveJoin(p);
    }

    public void connectMain(final ProxiedPlayer p) {
        ServerInfo info = ProxyServer.getInstance().getServerInfo("main");
        if (info == null) {
            p.disconnect(new ComponentBuilder("Tous les lobbys sont actuellement pleins.").create());
            return;
        }
        p.connect(info, new Callback<Boolean>() {
            @Override
            public void done(Boolean aBoolean, Throwable throwable) {
                if (!aBoolean) {
                    p.disconnect(new ComponentBuilder("Tous les lobbys sont actuellement pleins.").create());
                }
            }
        });
    }

    public LobbyServer getBestLobby()
    {
        LobbyServer server;
        server = getRandomLobbyByLevel(LobbyServer.Level.MEDIUM);
        if(server != null)
        {
            return server;
        }

        server = getRandomLobbyByLevel(LobbyServer.Level.CHARGED);
        if(server != null)
        {
            return server;
        }

        server = getRandomLobbyByLevel(LobbyServer.Level.LOW);
        if(server != null)
        {
            return server;
        }

        server = getRandomLobbyByLevel(LobbyServer.Level.FULL);
        if(server != null)
        {
            return server;
        }

        return null;
    }

    public LobbyServer getFirstLobbyByLevel(LobbyServer.Level level)
    {
        for(LobbyServer server : lobbyData.servers)
        {
            if(server.isOnline() && server.getLevel().equals(level))
            {
                return server;
            }
        }
        return null;
    }

    public LobbyServer getRandomLobbyByLevel(LobbyServer.Level level)
    {
        List<LobbyServer> servers = getLobbyByLevel(level);
        if(servers.size() <= 0)
            return null;

        return servers.get(random.nextInt(servers.size()));
    }

    public List<LobbyServer> getLobbyByLevel(LobbyServer.Level level)
    {
        List<LobbyServer> result = lobbyData.servers.stream().filter(server -> server.isOnline() && server.getLevel().equals(level)).collect(Collectors.toList());
		return result;
    }



}
