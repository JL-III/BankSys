package com.nessxxiii.banksys.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Instant> cooldownMap;
    private final Duration cooldownDuration;

    public CooldownManager(Duration cooldownDuration) {
        this.cooldownMap = new HashMap<>();
        this.cooldownDuration = cooldownDuration;
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

    public void updateCooldown(UUID uuid) {
        cooldownMap.put(uuid, Instant.now());
    }
}
