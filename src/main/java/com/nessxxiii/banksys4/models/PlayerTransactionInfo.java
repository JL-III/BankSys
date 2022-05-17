package com.nessxxiii.banksys4.models;

public class PlayerTransactionInfo {

    private String name;
    private String playerUUID;
    private int dataBaseBalNew;


    public PlayerTransactionInfo(String playerUUID, int dataBaseBalNew) {
        this.playerUUID = playerUUID;
        this.dataBaseBalNew = dataBaseBalNew;
    }

    public int getBalance() {
        return dataBaseBalNew;
    }
    public String getName() {
        return name;
    }

}
