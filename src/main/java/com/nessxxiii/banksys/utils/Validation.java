package com.nessxxiii.banksys.utils;

import java.util.Optional;

public class Validation {

    public static Optional<Integer> processPlayerInputAmount(String arg) {
        Optional<Integer> amount;
        try {
            amount = Optional.of(Integer.parseInt(arg));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        if (amount.get() <= 0) {
            return Optional.empty();
        }
        return amount;
    }

}
