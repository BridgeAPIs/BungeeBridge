package net.zyuiop.bungeebridge.lobbys;

import java.util.List;
import java.util.UUID;
// TODO : Rewrite this file as we don't own it

public class LobbyServer implements Comparable{

	private List<UUID> connectedPlayers;
    private int maxPlayers;
    private boolean online;
    private String serverName;

    private Level level;

    public LobbyServer(String serverName, boolean online, List<UUID> connectedPlayers, int maxPlayers, Level level) {
        this.connectedPlayers = connectedPlayers;
        this.maxPlayers = maxPlayers;
        this.online = online;
        this.serverName = serverName;
        this.level = level;
    }

    public Level getLevel()
    {
        return level;
    }

    public void setLevel(Level level)
    {
        this.level = level;
    }

    public List<UUID> getConnectedPlayers() {
        return connectedPlayers;
    }

    public void setConnectedPlayers(List<UUID> connectedPlayers) {
        this.connectedPlayers = connectedPlayers;
    }

    public int getPlayerCount()
    {
        return connectedPlayers.size();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int compareTo(Object o) {
        LobbyServer other = (LobbyServer)o;
        return serverName.compareTo(other.getServerName());
    }

    public enum Level{
        LOW(25),
        MEDIUM(40),
        CHARGED(15),
        FULL(85),
        EMERGENCY(95);

        private int pourcentage;
        private Level(int pourcentage)
        {
            this.pourcentage = pourcentage;
        }

        public static Level getGoodLevel(int pourcentage)
        {
            if(pourcentage <= Level.LOW.getLevel())
            {
                return Level.LOW;
            }

            if(pourcentage <= Level.MEDIUM.getLevel())
            {
                return Level.MEDIUM;
            }

            if(pourcentage <= Level.CHARGED.getLevel())
            {
                return Level.CHARGED;
            }

            if(pourcentage <= Level.FULL.getLevel())
            {
                return Level.FULL;
            }

            return Level.EMERGENCY;
        }

        public int getLevel()
        {
            return pourcentage;
        }
    }

}
