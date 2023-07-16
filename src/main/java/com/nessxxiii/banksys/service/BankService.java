package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionType;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class BankService {

    // Method for processing transactions in the bank system (database)
    // Side effect: May update player balance in the database and log the transaction
    public static Optional<Integer> processBankTransaction(PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger, UUID playerUUID, int amount, TransactionType transactionType, int oldBankBalOpt) {
        try {
            switch (transactionType) {
                case DEPOSIT -> {
                    // Update player balance in database
                    return Optional.of(playerBalanceDAO.updatePlayerBalance(playerUUID, amount));
                }
                case WITHDRAWAL -> {
                    if (oldBankBalOpt < amount) {
                        // Log insufficient funds in database
                        customLogger.sendLog("Player does not have sufficient bank balance to withdraw " + amount);
                        return Optional.empty();
                    }
                    // Update player balance in database
                    return Optional.of(playerBalanceDAO.updatePlayerBalance(playerUUID, -amount));
                }
                default -> {
                    return Optional.empty();
                }
            }
        } catch (SQLException ex) {
            // Log failure to update balance in database
            customLogger.sendLog("Failed to update bank balance for player " + playerUUID + " during " + transactionType);
            ex.printStackTrace();
            return Optional.empty();
        }
    }

}
