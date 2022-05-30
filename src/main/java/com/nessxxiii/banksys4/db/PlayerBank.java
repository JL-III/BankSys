package com.nessxxiii.banksys4.db;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.models.PlayerBalance;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlayerBank {
    private final Database database;

    public PlayerBank(Banksys4 plugin) {
        this.database = plugin.getDatabase();
    }

    public void initialize() throws SQLException {
        Statement statement = database.getConnection().createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS player_bank(playerUUID varchar(50) primary key,balance INT(30))");
        statement.close();
    }

    public List<PlayerBalance> getBalances() throws SQLException {
        List<PlayerBalance> result = new ArrayList<>();

        PreparedStatement statement = database.getConnection().prepareStatement("SELECT playerUUID, balance FROM player_bank WHERE balance > 1");
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            PlayerBalance balance = new PlayerBalance();
            balance.setUuid(rs.getString("playerUUID"));
            balance.setBalance(rs.getInt("balance"));
            result.add(balance);
        }

        return result;
    }

    public Integer findPlayerBalance(String playerUUID) throws SQLException {
        Integer balance = null;

        PreparedStatement statement = database.getConnection().prepareStatement("SELECT balance FROM player_bank WHERE playerUUID = ?");
        statement.setString(1, playerUUID);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            balance = resultSet.getInt("balance");
        }

        statement.close();
        resultSet.close();
        return balance;
    }

    public void createPlayerBalanceIfNotExists(String playerUUID) throws SQLException {
        if (findPlayerBalance(playerUUID) != null) return;
        PreparedStatement statement = database.getConnection().prepareStatement("INSERT INTO player_bank(playerUUID,balance) VALUES (?,?)");
        statement.setString(1, playerUUID);
        statement.setInt(2, 0);
        statement.executeUpdate();
        statement.close();
        Bukkit.getServer().getConsoleSender().sendMessage("Created entry in BankSys2 for " + playerUUID);
    }

    public void updatePlayerBalance(String playerUUID, int amount) throws SQLException {
        Integer currentBalance = findPlayerBalance(playerUUID);
        Integer newBalance = currentBalance + amount;

        PreparedStatement statement = database.getConnection().prepareStatement("UPDATE player_bank SET balance = ? WHERE playerUUID = ?");
        statement.setInt(1, newBalance);
        statement.setString(2, playerUUID);

        statement.executeUpdate();
        statement.close();
    }
}
