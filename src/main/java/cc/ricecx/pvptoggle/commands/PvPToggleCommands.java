package cc.ricecx.pvptoggle.commands;


import cc.ricecx.pvptoggle.PvPConfig;
import cc.ricecx.pvptoggle.PvPToggle;
import cc.ricecx.pvptoggle.util.ConfigHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;

import java.time.Duration;
import java.util.UUID;

@Command({"pvptoggle", "pvp"})
public class PvPToggleCommands {

    private final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build(); // every 10 minutes clear.

    @Default
    public void help(BukkitCommandActor actor) {
        final Player player = actor.requirePlayer();
        ConfigHelper.send("help-header", player);
        ConfigHelper.send("help-general-usage", player);

        if (player.hasPermission("pvptoggle.others")) {
            ConfigHelper.send("help-view-others", player);
            ConfigHelper.send("help-set-others", player);
        }
    }

    @Subcommand("toggle")
    public void togglePvP(BukkitCommandActor actor) {
        final Player player = actor.requirePlayer();

        if (!isCooldown(player, true)) {
            boolean newState = PvPToggle.config().toggleState(player);

            if (newState) {
                ConfigHelper.send("pvp-state-enabled", player);
            } else {
                ConfigHelper.send("pvp-state-disabled", player);
            }
            cooldown.put(player.getUniqueId(), System.currentTimeMillis());
        }

    }

    @Subcommand("on")
    public void setOn(BukkitCommandActor actor) {
        final Player player = actor.requirePlayer();
        PvPConfig pvp = PvPToggle.config();

        if (pvp.getPvPState(player)) {
            ConfigHelper.send("pvp-state-already-enabled", player);
        } else {
            if (!isCooldown(player, true)) {
                pvp.setPvPState(player, true);
                ConfigHelper.send("pvp-state-enabled", player);
                cooldown.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @Subcommand("off")
    public void setOff(BukkitCommandActor actor) {
        final Player player = actor.requirePlayer();
        PvPConfig pvp = PvPToggle.config();

        if (!pvp.getPvPState(player)) {
            ConfigHelper.send("pvp-state-already-disabled", player);
        } else {
            pvp.setPvPState(player, false);
            ConfigHelper.send("pvp-state-disabled", player);
            cooldown.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Subcommand("status")
    public void getStatus(BukkitCommandActor actor) {
        final Player player = actor.requirePlayer();

        boolean isPvPEnabled = PvPToggle.config().getPvPState(player);

        if (isPvPEnabled) {
            ConfigHelper.send("pvp-enabled", player);
        } else {
            ConfigHelper.send("pvp-disabled", player);
        }
    }

    private boolean isCooldown(Player player, boolean sendMessage) {
        UUID uuid = player.getUniqueId();

        if (cooldown.asMap().containsKey(uuid)) {
            Long configCooldown = ConfigHelper.cooldown();

            Long cooldownValue = cooldown.getIfPresent(uuid);
            if (cooldownValue == null) cooldownValue = 0L;

            long leftMillis = System.currentTimeMillis() - cooldownValue;
            long seconds = Duration.ofMillis(leftMillis).toSeconds();
            long secondsLeft = configCooldown - seconds;

            if (secondsLeft <= 0) {
                cooldown.invalidate(uuid);
                return false;
            }

            if (sendMessage) ConfigHelper.send("pvp-cooldown", player, secondsLeft);

            return true;
        } else return false;
    }
}
