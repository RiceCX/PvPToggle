package cc.ricecx.pvptoggle;

import cc.ricecx.pvptoggle.commands.PvPToggleCommands;
import cc.ricecx.pvptoggle.hooks.PlaceholderApiHook;
import cc.ricecx.pvptoggle.listeners.ConnectionListener;
import cc.ricecx.pvptoggle.listeners.PvpListener;
import cc.ricecx.pvptoggle.listeners.WorldListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public class PvPToggle extends JavaPlugin {

    private PvPConfig pvpConfig;


    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        pvpConfig = new PvPConfig(this);

        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new PvPToggleCommands());
        handler.registerBrigadier();

        getServer().getPluginManager().registerEvents(new PvpListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(), this);

        /* Hooks */
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderApiHook().register();
            getLogger().info("Hooked into PlaceholderAPI!");
        }
    }

    public static PvPConfig config() {
        return get().pvpConfig;
    }

    public static FileConfiguration bukkitConfig() {
        return get().getConfig();
    }

    public static PvPToggle get() {
        return JavaPlugin.getPlugin(PvPToggle.class);
    }
}
