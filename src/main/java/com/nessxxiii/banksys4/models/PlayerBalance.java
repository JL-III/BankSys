package com.nessxxiii.banksys4.models;

public class PlayerBalance {

    private String name;
    private String playerUUID;
    private int dataBaseBalance;

    private int essentialsBalance;
    private int requestedAmount;

    public PlayerBalance(String playerUUID, int dataBaseBalance) {
        this.playerUUID = playerUUID;
        this.dataBaseBalance = dataBaseBalance;
    }

    public PlayerBalance(String name, String playerUUID, int dataBaseBalance, int essentialsBalance) {
        this.name = name;
        this.playerUUID = playerUUID;
        this.dataBaseBalance = dataBaseBalance;
        this.essentialsBalance = essentialsBalance;
    }

    public PlayerBalance(String playerUUID, int dataBaseBalance, int essentialsBalance, int requestedAmount) {
        this.playerUUID = playerUUID;
        this.dataBaseBalance = dataBaseBalance;
        this.essentialsBalance = essentialsBalance;
        this.requestedAmount = requestedAmount;

    }


    public String getPlayerUUID() {this.playerUUID = playerUUID; return playerUUID; }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int getBalance() {
        return dataBaseBalance;
    }

    public void setBalance(int balance) {
        this.dataBaseBalance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
