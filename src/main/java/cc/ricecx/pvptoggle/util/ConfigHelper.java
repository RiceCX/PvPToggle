package cc.ricecx.pvptoggle.util;

import cc.ricecx.pvptoggle.PvPToggle;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfigHelper {

    public static boolean isPersist() {
        return PvPToggle.bukkitConfig().getBoolean("settings.persist-pvp-state");
    }

    public static Long cooldown() {
        return PvPToggle.bukkitConfig().getLong("settings.cooldown");
    }

    public static boolean defaultPvPState() {
        return PvPToggle.bukkitConfig().getBoolean("settings.default-pvp-state");
    }

    public static List<String> blockedWorlds() {
        return PvPToggle.bukkitConfig().getStringList("settings.blocked-worlds");
    }

    public static void send(String path, Player player, Object... args) {
        String string = PvPToggle.bukkitConfig().getString("messages." + path, path);

        if (args.length > 0) string = String.format(string, args);

        String s = ChatColor.translateAlternateColorCodes('&', string);
        player.sendMessage(s);
    }
}
