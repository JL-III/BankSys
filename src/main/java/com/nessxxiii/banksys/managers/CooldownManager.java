package com.nessxxiii.banksys.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private ConfigManager configManager;
    private final Map<UUID, Instant> cooldownMap;
    private final Duration cooldownDuration;

    public CooldownManager(ConfigManager configManager) {
        this.cooldownMap = new HashMap<>();
        this.configManager = configManager;
        this.cooldownDuration = configManager.getCooldown();
    }

    public boolean isCooldownOver(UUID uuid) {
        Instant now = Instant.now();
        if (cooldownMap.containsKey(uuid)) {
            Instant lastUse = cooldownMap.get(uuid);
            return Duration.between(lastUse, now).compareTo(cooldownDuration) >= 0;
        } else {
            return true;
        }
    }

    public Duration getNextUse(UUID uuid) {
        if (cooldownMap.containsKey(uuid)) {
            return Duration.between(cooldownMap.get(uuid), Instant.now());
        } else {
            return null;
        }
    }

    public void updateCooldown(UUID uuid) {
        cooldownMap.put(uuid, Instant.now());
    }
}
