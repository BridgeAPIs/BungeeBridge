package net.bridgesapis.bungeebridge.i18n;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class I18n {

    private static Configuration lang;

    public static void load(String lang, BungeeBridge plugin) throws IOException {
        File file = new File(plugin.getDataFolder().getPath() + "/i18n/" + lang + ".lang");
        if (!file.exists()) {
            InputStream stream = plugin.getResourceAsStream(lang + ".lang");
            if (stream == null) {
                stream = plugin.getResourceAsStream("default.lang");
                lang = "default";
            }

            file = new File(plugin.getDataFolder().getPath() + "/i18n/" + lang + ".lang");
            if (!new File(plugin.getDataFolder().getPath() + "/i18n/").exists())
                new File(plugin.getDataFolder().getPath() + "/i18n/").mkdir();

            Files.copy(stream, file.toPath());
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

    public static String getModuleTranslation(String module, String key) {
        return getTranslation("modules." + module + "." + key);
    }

	public static String getWord(String word) {
		return getTranslation("words." + word);
	}

	public static String getTag() {
		return getTranslation("plugin_tag");
	}
}
