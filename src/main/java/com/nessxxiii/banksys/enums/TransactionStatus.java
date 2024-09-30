package com.nessxxiii.banksys.enums;

public enum TransactionStatus {
    SUCCESS("Success"),
    INSUFFICIENT_FUNDS("Insufficient Funds"),
    FAILURE("Something failed");

    private final String name;

    TransactionStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
