package com.nessxxiii.banksys4.db;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.ObjectInputFilter;
import java.sql.*;

public class Database {

    private ConfigManager configManager;
    private Connection connection;

    public Database(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && connection.isValid(2)) {
            return connection;
        }

        connection = DriverManager.getConnection(configManager.getURL(), configManager.getUSER(), configManager.getPASSWORD());
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "BankSys: Initialized Connection");
        return connection;
    }
}
