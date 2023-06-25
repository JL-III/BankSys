package com.nessxxiii.banksys;

import com.nessxxiii.banksys.commands.PlayerCommands;
import com.nessxxiii.banksys.db.DBConnectionManager;
import com.nessxxiii.banksys.db.PlayerBalanceDAO;
import com.nessxxiii.banksys.managers.ConfigManager;
import com.nessxxiii.banksys.managers.TransactionManager;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class BankSys extends JavaPlugin {
    private final CustomLogger customLogger = new CustomLogger(this.getName(), NamedTextColor.GREEN, NamedTextColor.YELLOW);
    private static Economy economy = null;
    private DBConnectionManager DBConnectionManager;

    private PlayerBalanceDAO playerBalanceDAO;

    private TransactionManager transactionManager;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        if (configManager.databaseConnectionValuesAreSet()) {
            if (!setupEconomy()) {
                customLogger.sendLog(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            try {
                this.DBConnectionManager = new DBConnectionManager(configManager);
                this.playerBalanceDAO = new PlayerBalanceDAO(this);
                this.playerBalanceDAO.initialize();
                this.transactionManager = new TransactionManager(economy, playerBalanceDAO);
                Objects.requireNonNull(getCommand("bank")).setExecutor(new PlayerCommands(transactionManager));
            } catch (SQLException ex) {
                ex.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            customLogger.sendLog("Database connection values are not set, this plugin will not do anything until they are set.");
        }
    }

    @Override
    public void onDisable() {
        customLogger.sendLog(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        if (this.DBConnectionManager != null) {
            try {
                this.DBConnectionManager.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

        economy = registeredServiceProvider.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }

    public DBConnectionManager getDBConnectionManager() {
        return DBConnectionManager;
    }
}
