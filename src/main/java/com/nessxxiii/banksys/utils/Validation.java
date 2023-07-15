package com.nessxxiii.banksys.utils;

public class Validation {

    public static int processPlayerInputAmount(String arg) {
        int amount;
        try {
            amount = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            return -1;
        }
        if (amount <= 0) {
            return -1;
        }
        return amount;
    }

}
