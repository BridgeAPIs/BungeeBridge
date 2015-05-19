package net.zyuiop.bungeebridge.permissions.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.samagames.permissionsapi.PermissionsAPI;
import net.samagames.permissionsapi.permissions.PermissionUser;

public class CommandRefresh extends Command {

    private PermissionsAPI api;

    public CommandRefresh(PermissionsAPI api) {
        super("refresh");
        this.api = api;
    }

    protected boolean canDo(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            PermissionUser u = api.getUser(((ProxiedPlayer) sender).getUniqueId());
            if (u.hasPermission("permissions.bungee.*") || u.hasPermission("permissions.*") || u.hasPermission("permissions.bungee.refresh"))
                return true;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!canDo(sender)) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire cela.");
            return;
        }
        api.getManager().refresh();
        sender.sendMessage(ChatColor.GREEN + "Le cache a bien été rechargé.");
    }
}
