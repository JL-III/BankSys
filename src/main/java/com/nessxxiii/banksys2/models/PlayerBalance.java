package com.nessxxiii.banksys2.models;

public class PlayerBalance {

    private static String playerUUID;
    private int balance;

    public PlayerBalance(String playerUUID, int balance) {
        this.playerUUID = playerUUID;
        this.balance = balance;
    }

    public static String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
