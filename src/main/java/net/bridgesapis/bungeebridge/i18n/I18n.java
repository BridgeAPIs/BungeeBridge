package net.bridgesapis.bungeebridge.i18n;

import com.google.common.io.ByteStreams;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

public class I18n {

    private static Configuration lang;

    public static void load(String lang, BungeeBridge plugin) throws IOException {
        File file = new File(plugin.getDataFolder().getPath() + "/i18n/" + lang + ".lang");
        if (!file.exists()) {
            file = new File(plugin.getDataFolder().getPath() + "/i18n/default.lang");
            if (!file.exists()) {
                new File(plugin.getDataFolder().getPath() + "/i18n/").mkdir();
                file.createNewFile();
                try (InputStream is = plugin.getResourceAsStream("default.lang");
                     OutputStream os = new FileOutputStream(file)) {
                     ByteStreams.copy(is, os);
                }
            }
        }

        I18n.lang = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    public static String getTranslation(String key) {
        if (lang == null)
            try {
                load("default", BungeeBridge.getInstance());
            } catch (IOException e) {
                e.printStackTrace();
            }
        return lang.getString(key, key);
    }
}
