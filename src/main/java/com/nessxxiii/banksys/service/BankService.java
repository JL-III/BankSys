package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionType;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.Response;

import java.util.UUID;

public class BankService {

    public static Response<Integer> processBankTransaction(PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger, UUID playerUUID, int amount, TransactionType transactionType, int oldBankBalOpt) {
        switch (transactionType) {
            case DEPOSIT -> {
                // Update player balance in database
                return playerBalanceDAO.updatePlayerBalance(playerUUID, amount);
            }
            case WITHDRAWAL -> {
                if (oldBankBalOpt < amount) {
                    // Log insufficient funds in database
                    customLogger.sendLog("Player does not have sufficient bank balance to withdraw " + amount);
                    return Response.failure("Player does not have sufficient bank balance to withdraw " + amount);
                }
                // Update player balance in database
                return playerBalanceDAO.updatePlayerBalance(playerUUID, -amount);
            }
            default -> {
                customLogger.sendLog("Transaction type did not match DEPOSIT or WITHDRAWAL");
                return Response.failure("Transaction type did not match DEPOSIT or WITHDRAWAL");
            }
        }
    }
}
