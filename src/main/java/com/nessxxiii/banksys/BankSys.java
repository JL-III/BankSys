package com.nessxxiii.banksys;

import com.nessxxiii.banksys.commands.PlayerCommands;
import com.nessxxiii.banksys.db.DBConnectionManager;
import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.managers.ConfigManager;
import com.nessxxiii.banksys.managers.CooldownManager;
import com.nessxxiii.banksys.transaction.TransactionService;
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
    private DBConnectionManager dBConnectionManager;
    private PlayerBalanceDAO playerBalanceDAO;
    private TransactionService transactionService;
    private ConfigManager configManager;
    private PlayerCommands playerCommands;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        if (configManager.databaseConnectionValuesAreSet()) {
            if (!setupEconomy()) {
                customLogger.sendLog("Disabled due to no Vault dependency found!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            try {
                dBConnectionManager = new DBConnectionManager(configManager);
                playerBalanceDAO = new PlayerBalanceDAO(dBConnectionManager, customLogger);
                playerBalanceDAO.initializeDatabase();
                transactionService = new TransactionService(economy, playerBalanceDAO, customLogger);
                playerCommands = new PlayerCommands(transactionService, new CooldownManager(configManager), customLogger);
                Objects.requireNonNull(getCommand("bank")).setExecutor(playerCommands);
                customLogger.sendLog("Successfully initialized!");
            } catch (Exception ex) {
                ex.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            customLogger.sendLog("Database connection values are not set, this plugin will not do anything until they are set.");
        }
    }

    @Override
    public void onDisable() {
        customLogger.sendLog(String.format("Disabled Version %s", getDescription().getVersion()));
        if (this.dBConnectionManager != null) {
            try {
                this.dBConnectionManager.getConnection().close();
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

}
