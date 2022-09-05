package cc.ricecx.pvptoggle;

import cc.ricecx.pvptoggle.util.MessageCache;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PvPConfig {

    private final JavaPlugin plugin;
    private final File dataFolder;

    private final HashMap<UUID, Boolean> playerStates = new HashMap<>();
    private final MessageCache messageCache = new MessageCache();

    public PvPConfig(JavaPlugin plugin) {
        this.plugin = plugin;

        dataFolder = new File(plugin.getDataFolder(), "data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public boolean toggleState(Player player) {
        boolean state = !playerStates.getOrDefault(player.getUniqueId(), false);
        playerStates.put(player.getUniqueId(), state);
        return state;
    }

    public void setPvPState(Player player, boolean state) {
        playerStates.put(player.getUniqueId(), state);
    }

    public boolean getPvPState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), uuid -> {
            File file = new File(this.dataFolder, player.getUniqueId() + ".yml");
            final boolean defaultState = PvPToggle.bukkitConfig().getBoolean("default-pvp-state", false);

            if (!file.exists()) return defaultState;

            FileConfiguration fileData = YamlConfiguration.loadConfiguration(file);

            return fileData.getBoolean("pvp", defaultState);
        });
    }

    public void save(Player player) {
        File file = new File(this.dataFolder, player.getUniqueId() + ".yml");

        try {
            FileConfiguration fileData = YamlConfiguration.loadConfiguration(file);

            fileData.set("pvp", playerStates.get(player.getUniqueId()));
            fileData.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveAndRemove(Player player) {
        save(player);
        playerStates.remove(player.getUniqueId());
    }

    public void removeState(Player player) {
        playerStates.remove(player.getUniqueId());
    }

    public MessageCache messageCache() {
        return messageCache;
    }
}
