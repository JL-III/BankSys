package com.nessxxiii.banksys4.db;

import com.nessxxiii.banksys4.Banksys4;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlayersActiveToday {
    private final Banksys4 plugin;

    public PlayersActiveToday(Banksys4 plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {
        Statement statement = plugin.getDatabase().getConnection().createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS players_active_today(uuid varchar(50) primary key, player_name varchar(50))");
        statement.close();
    }

    public boolean exists(String uuid) throws SQLException {
        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("SELECT uuid FROM players_active_today WHERE uuid = ?");
        statement.setString(1, uuid);
        ResultSet resultSet = statement.executeQuery();

        boolean result = resultSet.next();

        statement.close();
        resultSet.close();

        return result;
    }

    public void upsert(String uuid, String name) throws SQLException {
        if (exists(uuid)) return;
        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("INSERT INTO players_active_today(uuid, player_name) VALUES (?,?)");
        statement.setString(1, uuid);
        statement.setString(2, name);
        statement.executeUpdate();
        statement.close();
    }

    public List<String> getActiveUUIDs() throws SQLException {
        List<String> results = new ArrayList<>();

        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("SELECT uuid FROM players_active_today");
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            results.add(rs.getString("uuid"));
        }

        statement.close();
        rs.close();

        return results;
    }

    public void purge() throws SQLException {
        PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("DELETE FROM players_active_today");
        statement.executeUpdate();
        statement.close();
    }
}
