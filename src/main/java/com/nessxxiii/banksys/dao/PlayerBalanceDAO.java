package com.nessxxiii.banksys.dao;

import com.nessxxiii.banksys.data.PlayerBalance;
import com.nessxxiii.banksys.db.DBConnectionManager;
import com.nessxxiii.banksys.exceptions.DatabaseOperationException;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerBalanceDAO {

    private final DBConnectionManager dbConnectionManager;
    private final CustomLogger customLogger;

    public PlayerBalanceDAO(DBConnectionManager dbConnectionManager, CustomLogger cUstomLogger) {
        this.dbConnectionManager = dbConnectionManager;
        this.customLogger = cUstomLogger;
    }

    public void initializeDatabase() throws DatabaseOperationException {
        try (Statement statement = dbConnectionManager.getConnection().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS player_bank(playerUUID varchar(50) primary key, balance INT(30))");
        } catch (SQLException e) {
            customLogger.sendLog("Exception while initializing database: " + e.getMessage());
            throw new DatabaseOperationException("Failed to initialize database!", e);
        }
    }

    public List<PlayerBalance> getBalances() throws SQLException {
        List<PlayerBalance> result = new ArrayList<>();
        try (PreparedStatement statement = dbConnectionManager.getConnection().prepareStatement("SELECT playerUUID, balance FROM player_bank WHERE balance > 1");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                PlayerBalance balance = new PlayerBalance();
                balance.setUuid(resultSet.getString("playerUUID"));
                balance.setBalance(resultSet.getInt("balance"));
                result.add(balance);
            }
        }
        return result;
    }

    public Optional<Integer> findPlayerBalance(UUID playerUUID) throws SQLException {
        Optional<Integer> balance = Optional.empty();

        try (PreparedStatement preparedStatement = dbConnectionManager.getConnection().prepareStatement("SELECT balance FROM player_bank WHERE playerUUID = ?")) {

            preparedStatement.setString(1, playerUUID.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    balance = Optional.of(resultSet.getInt("balance"));
                }
            }
        }
        return balance;
    }

    public boolean createPlayerBalanceIfNotExists(UUID playerUUID) throws RuntimeException {
        try {
            try (PreparedStatement preparedStatement = dbConnectionManager.getConnection().prepareStatement("INSERT IGNORE INTO player_bank(playerUUID,balance) VALUES (?,?)")) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setInt(2, 0);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    customLogger.sendLog("Player balance was found for " + playerUUID + " not creating new entry.");
                    return false;
                }
                customLogger.sendLog("Created entry in BankSys for " + playerUUID);
                return true;
            } catch (SQLException e) {
                customLogger.sendLog("Failed to update entry for " + playerUUID);
                throw new DatabaseOperationException("Failed to update entry!", e);
            }
        } catch (DatabaseOperationException e) {
            customLogger.sendLog("Exception while trying to create entry for " + playerUUID);
            throw new RuntimeException("Error while trying to create player balance", e);
        }
    }


    public void updatePlayerBalance(UUID playerUUID, int amount) throws SQLException {
        Optional<Integer> currentBalance = findPlayerBalance(playerUUID);
        Integer newBalance;
        if (currentBalance.isPresent()) {
            newBalance = currentBalance.get() + amount;
            try (PreparedStatement preparedStatement = dbConnectionManager.getConnection().prepareStatement("UPDATE player_bank SET balance = ? WHERE playerUUID = ?")) {
                preparedStatement.setInt(1, newBalance);
                preparedStatement.setString(2, playerUUID.toString());
                preparedStatement.executeUpdate();
            }
        }

    }
}