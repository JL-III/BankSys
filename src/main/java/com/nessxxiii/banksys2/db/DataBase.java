package com.nessxxiii.banksys2.db;
import com.nessxxiii.banksys2.models.PlayerBalance;
import org.bukkit.Bukkit;

import java.sql.*;

public class DataBase {

    private Connection connection;

    public Connection getConnection() throws SQLException{

        if(connection != null) {
            return connection;
        }
        //HOWTO SET IN CONFIG
        String url = "jdbc:mysql://cichlid.bloom.host:3306/s12917_bank_sys";
        String user = "u12917_yzg8DHopof";
        String password = "0i1B7yuYi4F^+v.7Lb1xNI11";

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

    public PlayerBalance findPlayerBalanceByUUID(String playerUUID) throws SQLException{

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM player_bank WHERE playerUUID = ?");
        statement.setString(1,playerUUID);
        ResultSet results = statement.executeQuery();

        if (results.next()){

            int balance = results.getInt("balance");

            PlayerBalance playerBalance = new PlayerBalance(playerUUID,balance);
            statement.close();
            return playerBalance;
        }
        statement.close();
        return null;
    }

    public void createPlayerBalance(String playerUUID) throws SQLException{

        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO player_bank(playerUUID,balance) VALUES (?,?)");

        statement.setString(1, playerUUID);
        statement.setInt(2,0);

        statement.executeUpdate();
        statement.close();
        Bukkit.getServer().getConsoleSender().sendMessage("Created entry in BankSys2 for " + playerUUID);
    }

    public void updatePlayerBalance(String playerUUID,int amount) throws SQLException{

        PlayerBalance playerBalance = findPlayerBalanceByUUID(playerUUID);
        int currentBalance = playerBalance.getBalance();
        int newBalance = currentBalance + amount;

        PreparedStatement statement = getConnection().prepareStatement("UPDATE player_bank SET balance = ? WHERE playerUUID = ?");

        statement.setInt(1, newBalance);
        statement.setString(2,playerUUID);

        statement.executeUpdate();
        statement.close();
    }

}
