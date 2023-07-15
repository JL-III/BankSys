package com.nessxxiii.banksys.managers;

import org.bukkit.plugin.Plugin;

import java.time.Duration;

public class ConfigManager {

    private final Plugin plugin;
    private String url;
    private String user;
    private String password;
    private boolean isMainServer;
    private Integer cooldown;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.url = plugin.getConfig().getString("url");
        this.user = plugin.getConfig().getString("user");
        this.password = plugin.getConfig().getString("password");
        this.isMainServer = plugin.getConfig().getBoolean("is-main-server");
        this.cooldown = plugin.getConfig().getInt("cooldown");
    }

    public void reloadConfigManager() {
        this.url = plugin.getConfig().getString("url");
        this.user = plugin.getConfig().getString("user");
        this.password = plugin.getConfig().getString("password");
        this.isMainServer = plugin.getConfig().getBoolean("is-main-server");
        this.cooldown = plugin.getConfig().getInt("cooldown");
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isMainServer() {
        return isMainServer;
    }

    //FIXME does not account for when keys don't exist at all.
    public boolean databaseConnectionValuesAreSet() {
        return getUrl().length() > 0 && getUser().length() > 0 && getPassword().length() > 0;
    }

    public Duration getCooldown() {
        return Duration.ofSeconds(cooldown);
    }
}
