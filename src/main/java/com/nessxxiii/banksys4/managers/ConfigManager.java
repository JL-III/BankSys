package com.nessxxiii.banksys4.managers;

import org.bukkit.plugin.Plugin;

public class ConfigManager {

    private final String URL;

    private final String USER;

    private final String PASSWORD;

    public ConfigManager(Plugin plugin) {
        this.URL = plugin.getConfig().getString("URL");
        this.USER = plugin.getConfig().getString("USER");
        this.PASSWORD = plugin.getConfig().getString("PASSWORD");
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
}
