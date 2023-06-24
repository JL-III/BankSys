package com.nessxxiii.banksys4;

import com.nessxxiii.banksys4.commands.PlayerCommands;
import com.nessxxiii.banksys4.db.Database;
import com.nessxxiii.banksys4.db.Bank;
import com.nessxxiii.banksys4.managers.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public final class Banksys4 extends JavaPlugin {
    private static Banksys4 plugin;
    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private Database database;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;
        Objects.requireNonNull(getCommand("bank")).setExecutor(new PlayerCommands(this));

        try {
            this.database = new Database(configManager);
            new Bank(this).initialize();
        } catch (SQLException ex) {
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));

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
