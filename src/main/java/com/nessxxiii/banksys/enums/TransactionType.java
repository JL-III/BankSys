package com.nessxxiii.banksys.enums;

public enum TransactionType {
    WITHDRAWAL("withdrawal"),
    DEPOSIT("deposit"),
    INQUIRY("inquiry");

    private final String name;

    TransactionType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
