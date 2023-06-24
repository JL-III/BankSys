package com.nessxxiii.banksys.db;

import com.nessxxiii.banksys.BankSys;
import com.nessxxiii.banksys.models.PlayerBalance;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Bank {

    private final BankSys plugin;

    public Bank(BankSys plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {
        Statement statement = plugin.getDatabase().getConnection().createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS player_bank(playerUUID varchar(50) primary key, balance INT(30)");
        statement.close();
    }

    public List<PlayerBalance> getBalances() throws SQLException {
        List<PlayerBalance> result = new ArrayList<>();
        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("SELECT playerUUID, balance FROM player_bank WHERE balance > 1");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            PlayerBalance balance = new PlayerBalance();
            balance.setUuid(resultSet.getString("playerUUID"));
            balance.setBalance(resultSet.getInt("balance"));
            result.add(balance);
        }

        return result;
    }

    public Integer findPlayerBalance(String playerUUID) throws SQLException {
        Integer balance = null;

        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("SELECT balance FROM player_bank WHERE playerUUID = ?");
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
        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("INSERT INTO player_bank(playerUUID,balance) VALUES (?,?)");
        statement.setString(1, playerUUID);
        statement.setInt(2, 0);
        statement.executeUpdate();
        statement.close();
        Bukkit.getServer().getConsoleSender().sendMessage("Created entry in BankSys2 for " + playerUUID);
    }

    public void updatePlayerBalance(String playerUUID, int amount) throws SQLException {
        Integer currentBalance = findPlayerBalance(playerUUID);
        Integer newBalance = currentBalance + amount;

        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("UPDATE player_bank SET balance = ? WHERE playerUUID = ?");
        statement.setInt(1, newBalance);
        statement.setString(2, playerUUID);

        statement.executeUpdate();
        statement.close();
    }
}