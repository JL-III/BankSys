package com.nessxxiii.banksys4.db;

import com.nessxxiii.banksys4.Banksys4;
import org.bukkit.Bukkit;

import java.sql.*;

public class Database {
    private final Banksys4 banksys4;

    public Database(Banksys4 banksys4) {
        this.banksys4 = banksys4;
    }

    private Connection connection;

    public Connection getConnection() throws SQLException {
        if (connection != null && connection.isValid(2)) {
            return connection;
        }

        String url = banksys4.getConfig().getString("URL");
        String user = banksys4.getConfig().getString("USER");
        String password = banksys4.getConfig().getString("PASSWORD");


        connection = DriverManager.getConnection(url, user, password);
        Bukkit.getServer().getConsoleSender().sendMessage("Banksys:");
        Bukkit.getServer().getConsoleSender().sendMessage("Initialized Connection");
        return connection;
    }
}
