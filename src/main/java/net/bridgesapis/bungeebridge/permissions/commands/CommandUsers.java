package net.bridgesapis.bungeebridge.permissions.commands;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.permissions.PermissionGroup;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class CommandUsers extends Command {

    private PermissionsAPI api = null;

    public CommandUsers(PermissionsAPI api) {
        super("users");
        this.api = api;
    }

    protected boolean canDo(CommandSender sender, String command, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            String basePerm = "permissions.bungee.users";
            PermissionUser u = api.getUser(((ProxiedPlayer) sender).getUniqueId());
            if (u.hasPermission("permissions.bungee.*") || u.hasPermission("permissions.*")) return true;
            if (u.hasPermission(basePerm + ".*")) return true;
            if (u.hasPermission(basePerm + "." + command + ".*")) return true;

            ArrayList<String> uniperm = new ArrayList<String>();
            uniperm.add("info");
            uniperm.add("allinfos");
            uniperm.add("help");
            uniperm.add("add");
            uniperm.add("del");
            uniperm.add("setoption");
            uniperm.add("deloption");

            if (uniperm.contains(command) && u.hasPermission(basePerm + "." + command))
                return true;
            else if (args != null && args.length != 0) {
                UUID player = getPlayerID(args[0]);
                return (u.hasPermission(basePerm + "." + command + "." + args[1]));
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public UUID getPlayerID(String name) {
        return BungeeBridge.getInstance().getUuidTranslator().getUUID(name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "SamaPermissions : version 0.1, BungeeCord edition. (c) zyuiop 2015.");
            return;
        }

        if (!canDo(sender, args[0], Arrays.copyOfRange(args, 1, args.length))) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire cela.");
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GOLD + "Aide de /users :");
            HashMap<String, String> com = new HashMap<>();
            com.put("addgroup <pseudo> <groupe> [durée]", "Ajoute le groupe a l'utilisateur (si durée définie : pendant [durée] sec)");
            com.put("setgroup <pseudo> <groupe> [durée]", "Définit le groupe de l'utilisateur (si durée définie : pendant [durée] sec)");
            com.put("delgroup <groupe> <groupe>", "Enlève le groupe à l'utilisateur");
            com.put("add <user> <permission>", "Définit une permission. -<perm> : interdiction");
            com.put("del <user> <permission>", "Enlève une permission/interdiction");
            com.put("setoption <user> <option> <valeur>", "Définit une option");
            com.put("deloption <user> <option>", "Supprime une option.");
            com.put("info <user>", "Affiche des infos sur l'utilisateur");
            com.put("allinfos <user>", "Affiche toutes les infos de l'utilisateur");

            for (String command : com.keySet()) {
                sender.sendMessage(ChatColor.GOLD + "/users " + command + ChatColor.WHITE + " : " + com.get(command));
            }
        } else {
            String command = args[0];
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Commande invalide : /users help pour plus d'infos");
                return;
            }

            if (command.equalsIgnoreCase("addgroup")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));
                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe parent n'existe pas");
                    return;
                }

                if (args.length == 4) {
                    int duration = Integer.decode(args[3]);
                    u.addParent(parent, duration);
                } else {
                    u.addParent(parent);
                }


                sender.sendMessage(ChatColor.GREEN + "Le groupe a été ajouté.");
            } else if (command.equalsIgnoreCase("setgroup")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));
                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe parent n'existe pas");
                    return;
                }

                ArrayList<PermissionGroup> gpes = new ArrayList<>();
                for (PermissionGroup gpe : u.getParents())
                    gpes.add(gpe);

                for (PermissionGroup gpe : gpes)
                    u.removeParent(gpe);

                if (args.length == 4) {
                    int duration = Integer.decode(args[3]);
                    u.addParent(parent, duration);
                } else {
                    u.addParent(parent);
                }

                sender.sendMessage(ChatColor.GREEN + "Le groupe a été défini.");
            } else if (command.equalsIgnoreCase("delgroup")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));
                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe parent n'existe pas");
                    return;
                }
                u.removeParent(parent);
                sender.sendMessage(ChatColor.GREEN + "Le groupe a été enlevé.");
            } else if (command.equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                // Ajout de permission.
                String permission = args[2];
                boolean value = !permission.startsWith("-");
                if (!value) permission = permission.substring(1); // On vire le "-"

                u.setPermission(permission, value);
                sender.sendMessage(ChatColor.GREEN + "La permission a été définie.");
            } else if (command.equalsIgnoreCase("setoption")) {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                String option = args[2];
                String value = args[3];

                u.setProperty(option, value);
                sender.sendMessage(ChatColor.GREEN + "L'option a été définie.");
            } else if (command.equalsIgnoreCase("deloption")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                u.deleteProperty(args[2]);
                sender.sendMessage(ChatColor.GREEN + "L'option a été supprimée.");
            } else if (command.equalsIgnoreCase("del")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                u.deletePermission(args[2]);
                sender.sendMessage(ChatColor.GREEN + "La permission a été supprimée");
            } else if (command.equalsIgnoreCase("info")) {
                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                sender.sendMessage(ChatColor.GOLD + "Joueur " + u.getEntityID());
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : u.getParents()) {
                    sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : u.getEntityPermissions().keySet()) {
                    net.md_5.bungee.api.chat.TextComponent component = new TextComponent(" => " + perm);
                    if (u.getPermissions().get(perm))
                        component.setColor(ChatColor.GREEN);
                    else
                        component.setColor(ChatColor.RED);
                    sender.sendMessage(component);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : u.getEntityProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + u.getProperty(option));
                }
            } else if (command.equalsIgnoreCase("allinfos")) {
                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                sender.sendMessage(ChatColor.GOLD + "Joueur " + u.getEntityID());
                sender.sendMessage(ChatColor.GOLD + "Infos complètes = perms et options héritées inclues.");
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : u.getParents()) {
                    sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : u.getPermissions().keySet()) {
                    net.md_5.bungee.api.chat.TextComponent component = new TextComponent(" => " + perm);
                    if (u.getPermissions().get(perm))
                        component.setColor(ChatColor.GREEN);
                    else
                        component.setColor(ChatColor.RED);
                    sender.sendMessage(component);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : u.getProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + u.getProperty(option));
                }
            }
        }
    }
}
