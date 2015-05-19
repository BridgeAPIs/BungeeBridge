package net.zyuiop.bungeebridge.permissions;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.zyuiop.bungeebridge.BungeeBridge;

import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private PermissionsBridge bridge;
    private BungeeBridge plugin;

    public PlayerListener(PermissionsBridge bridge, BungeeBridge plugin) {
        this.bridge = bridge;
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(final PostLoginEvent e) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
			bridge.getApi().getManager().refreshPerms(e.getPlayer().getUniqueId());
			bridge.logInfo("[PostLoginEvent] Applied permissions for player " + e.getPlayer().getUniqueId());
		}, 2, TimeUnit.SECONDS);
        bridge.logInfo("[PostLoginEvent] Registered schedule for " + e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent e) {
        e.registerIntent(plugin);
        bridge.logInfo("[PreLoginEvent] Loading permissions for player " + e.getConnection().getUniqueId());
        bridge.getApi().getManager().getUser(e.getConnection().getUniqueId()); // On charge les permisisons
        e.completeIntent(plugin);
    }
}
