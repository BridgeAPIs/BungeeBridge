package net.bridgesapis.bungeebridge.permissions.commands;

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


public class CommandGroups extends Command {
    private PermissionsAPI api = null;

    public CommandGroups(PermissionsAPI api) {
        super("groups");
        this.api = api;
    }

    protected boolean canDo(CommandSender sender, String command, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            String basePerm = "permissions.bungee.groups";
            PermissionUser u = api.getUser(((ProxiedPlayer) sender).getUniqueId());
            if (u.hasPermission("permissions.bungee.*") || u.hasPermission("permissions.*")) return true;
            if (u.hasPermission(basePerm + ".*")) return true;
            if (u.hasPermission(basePerm + "." + command + ".*")) return true;

            ArrayList<String> uniperm = new ArrayList<String>();
            uniperm.add("list");
            uniperm.add("create");
            uniperm.add("info");
            uniperm.add("allinfos");
            uniperm.add("help");

            if (uniperm.contains(command) && u.hasPermission(basePerm + "." + command))
                return true;
            else if (args != null && args.length != 0)
                return (u.hasPermission(basePerm + "." + command + "." + args[0]));
            else {
                return false;
            }
        } else {
            return true;
        }
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

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GOLD + "Aide de /groups :");
            HashMap<String, String> com = new HashMap<>();
            com.put("list", "liste les groupes enregistrés dans le cache.");
            com.put("create <groupe> <rank>", "crée le groupe");
            com.put("addparent <groupe> <parent>", "Ajoute le groupe <parent> dans les parents de <groupe>");
            com.put("delparent <groupe> <parent>", "Enlève <parent> des parents de <groupe>");
            com.put("deletegroup <group>", "Supprime le groupe");
            com.put("add <group> <permission>", "Définit une permission. -<perm> : interdiction");
            com.put("del <group> <permission>", "Enlève une permission/interdiction");
            com.put("setoption <group> <option> <valeur>", "Définit une option");
            com.put("deloption <group> <option>", "Supprime une option.");
            com.put("rename <grouo> <new name>", "Renomme le groupe");
            com.put("info <group>", "Affiche des infos sur le groupe");
            com.put("allinfos <group>", "Affiche toutes les infos du groupe");

            for (String command : com.keySet()) {
                sender.sendMessage(ChatColor.GOLD + "/groups " + command + ChatColor.WHITE + " : " + com.get(command));
            }
        } else {
            String command = args[0];

            if (command.equalsIgnoreCase("list")) {
                for (PermissionGroup g : api.getManager().getGroupsCache().values()) {
                    sender.sendMessage(g.getGroupName() + " (Rank : " + g.getLadder() + ") ");
                }
                return;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Commande invalide : /groups help pour plus d'infos");
                return;
            }

            if (command.equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "/groups create <nom> <rank>");
                    return;
                }
                if (api.getManager().getGroup(args[1]) != null) {
                    sender.sendMessage(ChatColor.RED + "Le groupe existe déjà.");
                    return;
                }

                PermissionGroup group = new PermissionGroup(api.getManager(), UUID.randomUUID(), Integer.decode(args[2]), args[1]);
                group.create(); // Sauvegarde le groupe
                api.getManager().refreshGroups();
            } else if (command.equalsIgnoreCase("addparent")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe parent n'existe pas");
                    return;
                }

                g.addParent(parent);
                sender.sendMessage(ChatColor.GREEN + "Le parent a été ajouté.");
            } else if (command.equalsIgnoreCase("delparent")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe parent n'existe pas");
                    return;
                }

                g.removeParent(parent);
                sender.sendMessage(ChatColor.GREEN + "Le parent a été supprimé.");
            } else if (command.equalsIgnoreCase("deletegroup")) {
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                g.remove();
                api.getManager().refreshGroups();
            } else if (command.equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                // Ajout de permission.
                String permission = args[2];
                boolean value = !permission.startsWith("-");
                if (!value) permission = permission.substring(1); // On vire le "-"

                g.setPermission(permission, value);
                sender.sendMessage(ChatColor.GREEN + "La permission a été définie.");
            } else if (command.equalsIgnoreCase("setoption")) {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                String option = args[2];
                String value = args[3];

                g.setProperty(option, value);
                sender.sendMessage(ChatColor.GREEN + "L'option a été définie.");
            } else if (command.equalsIgnoreCase("deloption")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                g.deleteProperty(args[2]);
                sender.sendMessage(ChatColor.GREEN + "L'option a été supprimée.");
            } else if (command.equalsIgnoreCase("rename")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                api.getManager().moveGroup(args[1], args[2]);
            } else if (command.equalsIgnoreCase("del")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Arguments manquants");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                g.deletePermission(args[2]);
                sender.sendMessage(ChatColor.GREEN + "La permission a été supprimée");
            } else if (command.equalsIgnoreCase("info")) {
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "Groupe " + g.getGroupName() + " (rank : " + g.getLadder() + ")");
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : g.getParents()) {
                    if (parent != null)
                        sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : g.getEntityPermissions().keySet()) {
                    net.md_5.bungee.api.chat.TextComponent component = new TextComponent(" => " + perm);
                    if (g.getPermissions().get(perm))
                        component.setColor(ChatColor.GREEN);
                    else
                        component.setColor(ChatColor.RED);
                    sender.sendMessage(component);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : g.getEntityProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + g.getProperty(option));
                }
            } else if (command.equalsIgnoreCase("allinfos")) {
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Erreur : le groupe n'existe pas");
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "Groupe " + g.getGroupName() + " (rank : " + g.getLadder() + ", id " + g.getEntityID() + ")");
                sender.sendMessage(ChatColor.GOLD + "Infos complètes = perms et options héritées inclues.");
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : g.getParents()) {
                    sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : g.getPermissions().keySet()) {
                    net.md_5.bungee.api.chat.TextComponent component = new TextComponent(" => " + perm);
                    if (g.getPermissions().get(perm))
                        component.setColor(ChatColor.GREEN);
                    else
                        component.setColor(ChatColor.RED);
                    sender.sendMessage(component);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : g.getProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + g.getProperty(option));
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Cette commande n'existe pas.");
            }
        }
    }
}
