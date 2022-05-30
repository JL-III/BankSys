package com.nessxxiii.banksys4;

import com.nessxxiii.banksys4.commands.BankCommands;
import com.nessxxiii.banksys4.db.Database;
import com.nessxxiii.banksys4.db.PlayerBank;
import com.nessxxiii.banksys4.db.PlayersActiveToday;
import com.nessxxiii.banksys4.listeners.ActivePlayersListener;
import com.nessxxiii.banksys4.services.BalanceTransfer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public final class Banksys4 extends JavaPlugin {
    private static Banksys4 plugin;
    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private Database database;
    private BalanceTransfer balanceTransfer;

    public static Banksys4 getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;
        Objects.requireNonNull(getCommand("bank")).setExecutor(new BankCommands(this));

        try {
            this.database = new Database(this);
            new PlayerBank(this).initialize();
            new PlayersActiveToday(this).initialize();
        } catch (SQLException ex) {
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        this.getServer().getPluginManager().registerEvents(new ActivePlayersListener(this), this);

        balanceTransfer = new BalanceTransfer(this);
        balanceTransfer.schedule();
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));

        try {
            this.database.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (this.balanceTransfer != null) {
            this.balanceTransfer.shutdown();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return econ;
    }

    public Database getDatabase() {
        return database;
    }
}
