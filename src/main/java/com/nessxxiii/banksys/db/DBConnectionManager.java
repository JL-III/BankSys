package com.nessxxiii.banksys.db;

import com.nessxxiii.banksys.managers.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
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
        config.setRegisterMbeans(true);
        config.setValidationTimeout(TimeUnit.SECONDS.toMillis(5));
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(30));
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        Bukkit.getConsoleSender().sendMessage("Active connections: " + poolMXBean.getActiveConnections());
        Bukkit.getConsoleSender().sendMessage("Idle connections: " + poolMXBean.getIdleConnections());
        Bukkit.getConsoleSender().sendMessage("Threads awaiting connection: " + poolMXBean.getThreadsAwaitingConnection());

        return dataSource.getConnection();
    }
}
