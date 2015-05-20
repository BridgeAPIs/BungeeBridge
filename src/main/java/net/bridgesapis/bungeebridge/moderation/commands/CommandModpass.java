package net.bridgesapis.bungeebridge.moderation.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.utils.Misc;
import org.apache.commons.lang3.RandomStringUtils;
import redis.clients.jedis.Jedis;

public class CommandModpass extends Command {
    public CommandModpass() {
        super("modpass", "modotools.modpass", "mdp");
    }

    @Override
    public void execute(final CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            final ProxiedPlayer player = (ProxiedPlayer) commandSender;
            final String id = player.getName();

            BungeeBridge.getInstance().getExecutor().addTask(() -> {
                // On vérifie la présence d'une clé dans la base de données
                Jedis jedis = BungeeBridge.getInstance().getConnector().getResource();
                String data = jedis.get("modpassword:" + id);
                long ttl = 0;
                if (data == null) {
                    data = RandomStringUtils.random(10, true, true);
                    jedis.set("modpassword:" + id, data);
                    jedis.expire("modpassword:" + id, 3600*24);
                    ttl = 3600 * 24;
                } else {
                    ttl = jedis.ttl("modpassword:" + id);
                }

                String ttlString = Misc.formatTime(ttl);
                TextComponent password = new TextComponent(data);
                password.setColor(ChatColor.YELLOW);

                TextComponent message = new TextComponent("Votre mot de passe est ");
                message.setColor(ChatColor.GREEN);
                message.addExtra(password);
                message.addExtra(". Il est utilisable pendant encore {2} sur ".replace("{2}", ttlString));

                TextComponent url = new TextComponent("l'interface de modération");
                url.setColor(ChatColor.AQUA);
                url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://samagames.net/modo/index.php/realm/login?username="+player.getName()+"&password="+data));
                message.addExtra(url);

                commandSender.sendMessage(message);
            });
        }
        //new RandomStringUtils().randomAlphanumeric(8);
    }
}
