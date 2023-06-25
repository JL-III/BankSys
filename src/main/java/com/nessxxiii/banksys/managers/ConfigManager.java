package com.nessxxiii.banksys.managers;

import org.bukkit.plugin.Plugin;

import java.time.Duration;

public class ConfigManager {

    private final String URL;

    private final String USER;

    private final String PASSWORD;
    private final Integer COOLDOWN;

    public ConfigManager(Plugin plugin) {
        this.URL = plugin.getConfig().getString("URL");
        this.USER = plugin.getConfig().getString("USER");
        this.PASSWORD = plugin.getConfig().getString("PASSWORD");
        this.COOLDOWN = plugin.getConfig().getInt("cooldown");
    }

    public String getURL() {
        return URL;
    }

    public String getUSER() {
        return USER;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public boolean databaseConnectionValuesAreSet() {
        if (getURL().length() > 0 && getUSER().length() > 0 && getPASSWORD().length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Duration getCooldown() {
        return Duration.ofSeconds(COOLDOWN);
    }
}
