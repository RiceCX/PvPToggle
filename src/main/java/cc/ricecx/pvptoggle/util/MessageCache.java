package cc.ricecx.pvptoggle.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.UUID;

public class MessageCache {

    private final Cache<UUID, UUID> cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(10)).build();

    public void addToCache(UUID attacker, UUID victim) {
        cache.put(attacker, victim);
    }

    public boolean isCached(UUID attacker, UUID victim) {
        return cache.getIfPresent(attacker) == victim;
    }

    public void clearCache() {
        cache.invalidateAll();
    }
}
