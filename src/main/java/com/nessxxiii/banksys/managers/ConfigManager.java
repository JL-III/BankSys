package com.nessxxiii.banksys.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private final Plugin plugin;
    private String url;
    private String user;
    private String password;
    private boolean isMainServer;
    private Integer cooldown;
    private String bankBalSelfPermission;
    private String bankBalOtherPermission;
    private String bankDepositPermission;
    private String bankWithdrawPermission;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void reloadConfigManager() {
        load();
    }

    private void load() {
        this.url = plugin.getConfig().getString("url");
        this.user = plugin.getConfig().getString("user");
        this.password = plugin.getConfig().getString("password");
        this.isMainServer = plugin.getConfig().getBoolean("is-main-server");
        this.cooldown = plugin.getConfig().getInt("cooldown");
        this.bankBalSelfPermission = plugin.getConfig().getString("bank-bal-self-permission");
        this.bankBalOtherPermission = plugin.getConfig().getString("bank-bal-other-permission");
        this.bankDepositPermission = plugin.getConfig().getString("bank-deposit-permission");
        this.bankWithdrawPermission = plugin.getConfig().getString("bank-withdraw-permission");
    }

    public String getUrl() {
        return url;
    }

    public String getBankBalOtherPermission() {
        return bankBalOtherPermission;
    }

    public String getBankBalSelfPermission() {
        return bankBalSelfPermission;
    }

    public String getBankDepositPermission() {
        return bankDepositPermission;
    }

    public String getBankWithdrawPermission() {
        return bankWithdrawPermission;
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
        Bukkit.getLogger().info(String.format("%b %b %b", getUrl().length() > 0, getUser().length() > 0, getPassword().length() > 0));

        return getUrl().length() > 0 && getUser().length() > 0 && getPassword().length() > 0;
    }

    public Duration getCooldown() {
        return Duration.ofSeconds(cooldown);
    }
}
