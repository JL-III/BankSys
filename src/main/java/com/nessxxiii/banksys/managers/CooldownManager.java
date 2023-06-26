package com.nessxxiii.banksys.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final ConfigManager configManager;
    private final Map<UUID, Instant> cooldownMap;

    public CooldownManager(ConfigManager configManager) {
        this.cooldownMap = new HashMap<>();
        this.configManager = configManager;
    }

    public boolean isCooldownOver(UUID uuid) {
        Instant now = Instant.now();
        if (cooldownMap.containsKey(uuid)) {
            Instant lastUse = cooldownMap.get(uuid);
            return Duration.between(lastUse, now).compareTo(configManager.getCooldown()) >= 0;
        } else {
            return true;
        }
    }

    public Long getNextUse(UUID uuid) {
        if (cooldownMap.containsKey(uuid)) {
            Duration timeSinceLastUse = Duration.between(cooldownMap.get(uuid), Instant.now());
            if (timeSinceLastUse.compareTo(configManager.getCooldown()) < 0) {
                return configManager.getCooldown().minus(timeSinceLastUse).toSeconds();
            } else {
                // Cooldown has already expired
                return Duration.ZERO.toSeconds();
            }
        } else {
            return 0L;
        }
    }

    public void updateCooldown(UUID uuid) {
        cooldownMap.put(uuid, Instant.now());
    }
}
