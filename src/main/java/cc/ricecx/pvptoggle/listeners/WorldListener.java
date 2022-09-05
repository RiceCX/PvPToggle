package cc.ricecx.pvptoggle.listeners;

import cc.ricecx.pvptoggle.PvPToggle;
import cc.ricecx.pvptoggle.util.ConfigHelper;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent evt) {
        final Player player = evt.getPlayer();
        final World world = player.getWorld();

        boolean isPlayerPvP = PvPToggle.config().getPvPState(player);

        if (!world.getPVP() && isPlayerPvP) {
            PvPToggle.config().setPvPState(player, false);
            ConfigHelper.send("pvp-world-change-disabled", player);
            return;
        }

        if (world.getPVP() && ConfigHelper.blockedWorlds().contains(world.getName()) && !isPlayerPvP) {
            PvPToggle.config().setPvPState(player, true);
            ConfigHelper.send("pvp-world-change-enabled", player);
        }
    }
}
