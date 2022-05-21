package com.nessxxiii.banksys4.db;
import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.models.PlayerTransactionInfo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;

public class DataBase {

    private final Banksys4 banksys4;

    public DataBase(Banksys4 banksys4){
        this.banksys4 = banksys4;
    }

    private Connection connection;

    public Connection getConnection() throws SQLException{

        if(connection != null) {
            return connection;
        }
        //HOWTO SET IN CONFIG
        String url = banksys4.getConfig().getString("URL");
        String user = banksys4.getConfig().getString("USER");
        String password = banksys4.getConfig().getString("PASSWORD");

        this.connection = DriverManager.getConnection(url,user,password);
        Bukkit.getServer().getConsoleSender().sendMessage("Connected to database!");
        return this.connection;

    }

    public void initializeDatabase() throws SQLException{

        Bukkit.getServer().getConsoleSender().sendMessage("inside of initializeDatabase");
        Statement statement = getConnection().createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS player_bank(playerUUID varchar(50) primary key,balance INT(30))";
        statement.execute(sql);
        statement.close();
/*        connection.close();*/
        Bukkit.getServer().getConsoleSender().sendMessage("Initialized Connection");
    }

    public PlayerTransactionInfo findPlayerBalance(String playerUUID) throws SQLException{

        PreparedStatement statement = getConnection().prepareStatement("SELECT balance FROM player_bank WHERE playerUUID = ?");
        statement.setString(1,playerUUID);
        ResultSet results = statement.executeQuery();

        if (results.next()){

            int balance = results.getInt("balance");

            PlayerTransactionInfo playerTransactionInfo = new PlayerTransactionInfo(playerUUID, balance);
            statement.close();
            return playerTransactionInfo;
        }
        statement.close();
        return null;
    }

    public void createPlayerBalanceIfNotExists(String playerUUID) throws SQLException{

        if (findPlayerBalance(playerUUID) != null) return;
        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO player_bank(playerUUID,balance) VALUES (?,?)");
        statement.setString(1, playerUUID);
        statement.setInt(2,0);
        statement.executeUpdate();
        statement.close();
        Bukkit.getServer().getConsoleSender().sendMessage("Created entry in BankSys2 for " + playerUUID);

    }

    public void updatePlayerBalance(String playerUUID,int amount) throws SQLException{

        PlayerTransactionInfo playerTransactionInfo = findPlayerBalance(playerUUID);
        int currentBalance = playerTransactionInfo.getBalance();
        int newBalance = currentBalance + amount;

        PreparedStatement statement = getConnection().prepareStatement("UPDATE player_bank SET balance = ? WHERE playerUUID = ?");
        statement.setInt(1, newBalance);
        statement.setString(2,playerUUID);

        statement.executeUpdate();
        statement.close();
    }

}
