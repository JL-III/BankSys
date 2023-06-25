package com.nessxxiii.banksys.db;

import com.nessxxiii.banksys.managers.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class DBConnectionManager {
    private final HikariDataSource dataSource;

    public DBConnectionManager(ConfigManager configManager) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configManager.getURL());
        config.setUsername(configManager.getUSER());
        config.setPassword(configManager.getPASSWORD());
        config.setValidationTimeout(TimeUnit.SECONDS.toMillis(5));
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(30));
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        Bukkit.getConsoleSender().sendMessage("Maximum: " + dataSource.getMaximumPoolSize());
        Bukkit.getConsoleSender().sendMessage("Maximum: " + dataSource.getDataSource().);
        return dataSource.getConnection();
    }
}
