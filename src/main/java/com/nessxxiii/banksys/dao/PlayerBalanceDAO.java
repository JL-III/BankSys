package com.nessxxiii.banksys.dao;

import com.nessxxiii.banksys.db.DBConnectionManager;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.Response;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

import static com.nessxxiii.banksys.utils.Formatter.getPlayerName;

public class PlayerBalanceDAO {

    private final DBConnectionManager dbConnectionManager;
    private final CustomLogger customLogger;

    public PlayerBalanceDAO(DBConnectionManager dbConnectionManager, CustomLogger cUstomLogger) {
        this.dbConnectionManager = dbConnectionManager;
        this.customLogger = cUstomLogger;
    }

    public void initializeDatabase() throws SQLException {
        try (Connection connection = dbConnectionManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS player_bank(playerUUID varchar(50) primary key, balance INT CHECK (balance >= 0))");
        }
    }

    public Response<Integer> findPlayerBalance(UUID playerUUID) {
        try (Connection connection = dbConnectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT balance FROM player_bank WHERE playerUUID = ?")) {

            preparedStatement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Optional<Integer> balance = Optional.of(resultSet.getInt("balance"));
                    if (balance.isPresent()) {
                        return Response.success(balance.get());
                    }
                    return Response.failure("balance was not present in findPlayerBalance method");
                }
                return Response.failure("resultSet.next() inside of `findPlayerBalance(UUID playerUUID)` was empty");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Response.failure(exception.getMessage());
        }
    }

    public void createPlayerBalanceIfNotExists(UUID playerUUID) throws SQLException {
        try (Connection connection = dbConnectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT IGNORE INTO player_bank(playerUUID,balance) VALUES (?,?)")) {
            preparedStatement.setString(1, playerUUID.toString());
            preparedStatement.setInt(2, 0);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                customLogger.sendLog("Player balance was found for " + getPlayerName(playerUUID) + " not creating new entry.");
                return;
            }
            customLogger.sendLog("Created entry in BankSys for " + getPlayerName(playerUUID));
        }
    }

    public Response<Integer> updatePlayerBalance(UUID playerUUID, int amount) {
        Response<Integer> integerResponseOriginalBalance = findPlayerBalance(playerUUID);
        int newBalance;
        if (integerResponseOriginalBalance.isSuccess()) {
            newBalance = integerResponseOriginalBalance.value() + amount;
            try (Connection connection = dbConnectionManager.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE player_bank SET balance = ? WHERE playerUUID = ?")) {
                preparedStatement.setInt(1, newBalance);
                preparedStatement.setString(2, playerUUID.toString());
                preparedStatement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
                customLogger.sendLog(exception.getMessage());
                return Response.failure(exception.getMessage());
            }
            Response<Integer> integerResponseUpdatedBalance = findPlayerBalance(playerUUID);
            if (integerResponseUpdatedBalance.isSuccess()) {
                return Response.success(integerResponseUpdatedBalance.value());
            }
            return Response.failure("Current balance was present but updated balance was not");
        }
        return Response.failure("There was an issue updating the player balance, integerResponseOriginalBalance was empty when called from updatePlayerBalance method.");
    }
}
