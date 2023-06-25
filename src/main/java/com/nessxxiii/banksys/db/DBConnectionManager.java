package com.nessxxiii.banksys.db;

import com.nessxxiii.banksys.managers.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class DBConnectionManager {
    private final HikariDataSource dataSource;

    public DBConnectionManager(ConfigManager configManager) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configManager.getURL());
        config.setUsername(configManager.getUSER());
        config.setPassword(configManager.getPASSWORD());

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
