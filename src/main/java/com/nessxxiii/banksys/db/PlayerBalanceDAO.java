package com.nessxxiii.banksys.db;

import com.nessxxiii.banksys.data.PlayerBalance;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerBalanceDAO {

    private final DBConnectionManager dbConnectionManager;
    private final CustomLogger customLogger;

    public PlayerBalanceDAO(DBConnectionManager dbConnectionManager, CustomLogger cUstomLogger) {
        this.dbConnectionManager = dbConnectionManager;
        this.customLogger = cUstomLogger;
    }

    public void initialize() throws SQLException {
        try (Connection connection = dbConnectionManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS player_bank(playerUUID varchar(50) primary key, balance INT(30))");
        }
    }

    public List<PlayerBalance> getBalances() throws SQLException {
        List<PlayerBalance> result = new ArrayList<>();
        PreparedStatement statement = dbConnectionManager.getConnection().prepareStatement("SELECT playerUUID, balance FROM player_bank WHERE balance > 1");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            PlayerBalance balance = new PlayerBalance();
            balance.setUuid(resultSet.getString("playerUUID"));
            balance.setBalance(resultSet.getInt("balance"));
            result.add(balance);
        }

        return result;
    }

    public Optional<Integer> findPlayerBalance(String playerUUID) throws SQLException {
        Optional<Integer> balance = Optional.empty();

        try (Connection connection = dbConnectionManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT balance FROM player_bank WHERE playerUUID = ?")) {

            preparedStatement.setString(1, playerUUID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    balance = Optional.of(resultSet.getInt("balance"));
                }
            }
        }
        return balance;
    }

    public void createPlayerBalanceIfNotExists(String playerUUID) throws SQLException {
        if (findPlayerBalance(playerUUID).isPresent()) return;
        PreparedStatement statement = dbConnectionManager.getConnection().prepareStatement("INSERT INTO player_bank(playerUUID,balance) VALUES (?,?)");
        statement.setString(1, playerUUID);
        statement.setInt(2, 0);
        statement.executeUpdate();
        statement.close();
        customLogger.sendLog("Created entry in BankSys2 for " + playerUUID);
    }

    public void updatePlayerBalance(String playerUUID, int amount) throws SQLException {
        Optional<Integer> currentBalance = findPlayerBalance(playerUUID);
        Integer newBalance;
        if (currentBalance.isPresent()) {
            newBalance = currentBalance.get() + amount;
            try (Connection connection = dbConnectionManager.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE player_bank SET balance = ? WHERE playerUUID = ?")) {
                preparedStatement.setInt(1, newBalance);
                preparedStatement.setString(2, playerUUID);
                preparedStatement.executeUpdate();
            }
        }

    }
}
