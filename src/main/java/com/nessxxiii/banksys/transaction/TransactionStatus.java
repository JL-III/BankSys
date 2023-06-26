package com.nessxxiii.banksys.transaction;

public enum TransactionStatus {
    SUCCESS("Success"),
    INSUFFICIENT_FUNDS("Insufficient Funds"),
    ERROR_E1("ErrorCode:E1"),
    ERROR_E2("ErrorCode:E2"),
    ERROR_E3("ErrorCode:E3"),
    ERROR_E4("ErrorCode:E4"),
    ERROR_E5("ErrorCode:E5");

    private final String name;

    TransactionStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
