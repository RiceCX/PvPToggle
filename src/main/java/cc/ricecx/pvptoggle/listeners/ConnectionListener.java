package cc.ricecx.pvptoggle.listeners;

import cc.ricecx.pvptoggle.PvPToggle;
import cc.ricecx.pvptoggle.util.ConfigHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    public ConnectionListener() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (ConfigHelper.isPersist()) {
                PvPToggle.config().getPvPState(onlinePlayer);
            } else {
                PvPToggle.config().setPvPState(onlinePlayer, ConfigHelper.defaultPvPState());
            }
        }
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        if (ConfigHelper.isPersist()) {
            PvPToggle.config().getPvPState(evt.getPlayer());
        } else {
            PvPToggle.config().setPvPState(evt.getPlayer(), ConfigHelper.defaultPvPState());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt) {
        if (ConfigHelper.isPersist()) {
            PvPToggle.config().saveAndRemove(evt.getPlayer());
        } else {
            PvPToggle.config().removeState(evt.getPlayer());
        }
    }
}
