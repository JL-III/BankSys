package com.nessxxiii.banksys4.db;

import com.nessxxiii.banksys4.Banksys4;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;

public class Database {

    private final String URL;
    private final String USER;
    private final String PASSWORD;
    private Connection connection;

    public Database(Banksys4 banksys4) {
        this.URL = banksys4.getConfig().getString("URL");
        this.USER = banksys4.getConfig().getString("USER");
        this.PASSWORD = banksys4.getConfig().getString("PASSWORD");
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && connection.isValid(2)) {
            return connection;
        }

        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "BankSys: Initialized Connection");
        return connection;
    }
}
