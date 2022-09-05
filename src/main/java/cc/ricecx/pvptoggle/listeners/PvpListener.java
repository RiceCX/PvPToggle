package cc.ricecx.pvptoggle.listeners;

import cc.ricecx.pvptoggle.PvPToggle;
import cc.ricecx.pvptoggle.util.ConfigHelper;
import cc.ricecx.pvptoggle.util.MessageCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;

public class PvpListener implements Listener {

    private final Cache<UUID, UUID> crystalDamage = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(5)).build();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void detectEndCrystal(EntityDamageByEntityEvent evt) {
        if (!(evt.getEntity() instanceof EnderCrystal crystal)) return;

        Entity damager = evt.getDamager();
        Player attacker = null;

        if (damager instanceof Player player) {
            attacker = player;
        } else if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                attacker = player;
            }
        }

        if (attacker == null) return;
        crystalDamage.put(crystal.getUniqueId(), attacker.getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent evt) {
        final Entity entity = evt.getEntity();
        if (ConfigHelper.blockedWorlds().contains(entity.getWorld().getName())) return;

        if (!(entity instanceof Player victim)) return;

        if (evt.getDamager() instanceof Player attacker) {
            boolean b = damageAllowed(attacker, victim, true);
            evt.setCancelled(!b);
            return;
        }

        if (evt.getDamager() instanceof EnderCrystal crystal) {
            UUID attacker = crystalDamage.getIfPresent(crystal.getUniqueId());
            if (attacker == null) return;
            Player player = Bukkit.getPlayer(attacker);
            if (player != null) {
                boolean b = damageAllowed(player, victim, true);
                evt.setCancelled(!b);
            }
        }

        if (evt.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                boolean b = damageAllowed(shooter, victim, true);
                evt.setCancelled(!b);
            }
            return;
        }

        if (evt.getDamager() instanceof LightningStrike && evt.getDamager().getMetadata("TRIDENT").size() >= 1) {
            if (!PvPToggle.config().getPvPState(victim)) {
                evt.setCancelled(true);
            }
        }

        if (evt.getDamager() instanceof Firework firework) {
            if (firework.getShooter() instanceof Player shooter) {
                boolean b = damageAllowed(shooter, victim, true);
                evt.setCancelled(!b);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent evt) {
        final Entity entity = evt.getEntity();
        if (ConfigHelper.blockedWorlds().contains(entity.getWorld().getName())) return;

        if (evt.getPotion().getShooter() instanceof Player shooter) {
            for (LivingEntity affectedEntity : evt.getAffectedEntities()) {
                if (affectedEntity instanceof Player victim) {
                    boolean b = damageAllowed(shooter, victim, true);
                    evt.setIntensity(victim, b ? evt.getIntensity(victim) : 0);
                }
            }
        }
    }

    @EventHandler
    public void onAreaEffect(AreaEffectCloudApplyEvent evt) {
        final Entity entity = evt.getEntity();
        if (ConfigHelper.blockedWorlds().contains(entity.getWorld().getName())) return;

        Iterator<LivingEntity> iterator = evt.getAffectedEntities().iterator();

        Player attacker = (Player) evt.getEntity().getSource();
        while (iterator.hasNext()) {
            LivingEntity affectedEntity = iterator.next();
            if (affectedEntity instanceof Player victim) {
                boolean b = damageAllowed(attacker, victim, true);
                if (!b) {
                    iterator.remove();
                }
            }
        }

    }

    @EventHandler
    public void onFlameArrow(EntityCombustByEntityEvent evt) {
        final Entity entity = evt.getEntity();
        if (ConfigHelper.blockedWorlds().contains(entity.getWorld().getName())) return;


        if (evt.getCombuster() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player attacker && evt.getEntity() instanceof Player victim) {
                boolean b = damageAllowed(attacker, victim, false);
                evt.setCancelled(!b);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent evt) {
        if (ConfigHelper.blockedWorlds().contains(evt.getPlayer().getWorld().getName())) return;

        if (evt.getCaught() instanceof Player victim) {
            boolean b = damageAllowed(evt.getPlayer(), victim, false);
            evt.setCancelled(!b);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLightningStrike(LightningStrikeEvent event) {
        if (event.getCause() == LightningStrikeEvent.Cause.TRIDENT) {
            event.getLightning().setMetadata("TRIDENT", new FixedMetadataValue(PvPToggle.get(), event.getLightning().getLocation()));
        }
    }

    private boolean damageAllowed(Player attacker, Player victim, boolean sendMessage) {
        if (attacker == victim) return true; // LMAO whoops forgot about this

        boolean attackerState = PvPToggle.config().getPvPState(attacker);
        boolean victimState = PvPToggle.config().getPvPState(victim);

        UUID attackerUUID = attacker.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        MessageCache mCache = PvPToggle.config().messageCache();

        if (!attackerState) {
            if (sendMessage) {
                if (!mCache.isCached(attackerUUID, victimUUID)) {
                    ConfigHelper.send("pvp-disabled", attacker);
                    mCache.addToCache(attackerUUID, victimUUID);
                }
            }
            return false;
        }

        if (!victimState) {
            if (sendMessage) {
                if (!mCache.isCached(attackerUUID, victimUUID)) {
                    ConfigHelper.send("pvp-disabled-others", attacker, victim.getDisplayName());
                    mCache.addToCache(attackerUUID, victimUUID);
                }
            }
            return false;
        }
        return true;
    }
}
