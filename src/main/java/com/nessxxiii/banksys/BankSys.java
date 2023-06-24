package com.nessxxiii.banksys;

import com.nessxxiii.banksys.commands.PlayerCommands;
import com.nessxxiii.banksys.db.Database;
import com.nessxxiii.banksys.db.Bank;
import com.nessxxiii.banksys.managers.ConfigManager;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public final class BankSys extends JavaPlugin {
    private final CustomLogger log = new CustomLogger(this.getName(), NamedTextColor.GREEN, NamedTextColor.YELLOW);
    private static Economy econ = null;
    private Database database;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        if (configManager.databaseConnectionValuesAreSet()) {
            if (!setupEconomy()) {
                log.sendLog(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            Objects.requireNonNull(getCommand("bank")).setExecutor(new PlayerCommands(this));
            try {
                this.database = new Database(configManager, log);
                new Bank(this).initialize();
            } catch (SQLException ex) {
                ex.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Database connection values are not set, this plugin will not do anything until they are set.");
        }
    }

    @Override
    public void onDisable() {
        log.sendLog(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));

        try {
            this.database.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> registeredServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredServiceProvider == null) {
            return false;
        }

        econ = registeredServiceProvider.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return econ;
    }

    public Database getDatabase() {
        return database;
    }
}
