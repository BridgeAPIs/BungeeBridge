package net.bridgesapis.bungeebridge.utils;

import net.bridgesapis.bungeebridge.BungeeBridge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsManager {

    public static Map<String, String> getSettings(UUID player) {
        Map<String, String> data = BungeeBridge.getInstance().getPlayerDataManager().getPlayerData(player).getValues();
        HashMap<String, String> settings = new HashMap<>();
        data.entrySet().stream().filter(line -> line.getKey().startsWith("settings.")).forEach(line -> {
            String setting = line.getKey().split(".")[0];
            settings.put(setting, line.getValue());
        });

        return settings;
    }

    public static String getSetting(UUID player, String setting) {
		return BungeeBridge.getInstance().getPlayerDataManager().getPlayerData(player).get("settings." + setting);
    }

    public static String getSetting(UUID player, String setting, String def) {
        String val = getSetting(player, setting);
        return (val == null) ? def : val;
    }

    public static boolean isEnabled(UUID player, String setting) {
        return BungeeBridge.getInstance().getPlayerDataManager().getPlayerData(player).getBoolean("settings." + setting);
    }

    public static boolean isEnabled(UUID player, String setting, boolean val) {
        return BungeeBridge.getInstance().getPlayerDataManager().getPlayerData(player).getBoolean("settings." + setting, val);
    }

    public static void setSetting(UUID player, String setting, String value) {
        BungeeBridge.getInstance().getPlayerDataManager().getPlayerData(player).set("settings." + setting, value);
    }
}
