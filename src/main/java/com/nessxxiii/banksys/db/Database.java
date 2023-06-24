package com.nessxxiii.banksys.db;

import com.nessxxiii.banksys.managers.ConfigManager;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;

public class Database {

    private ConfigManager configManager;
    private CustomLogger customLogger;
    private Connection connection;

    public Database(ConfigManager configManager, CustomLogger customLogger) {
        this.configManager = configManager;
        this.customLogger = customLogger;
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && connection.isValid(2)) {
            return connection;
        }

        connection = DriverManager.getConnection(configManager.getURL(), configManager.getUSER(), configManager.getPASSWORD());
        customLogger.sendLog("Initialized Connection.");
        return connection;
    }
}
