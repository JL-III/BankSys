package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import com.nessxxiii.banksys.logging.TransactionLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.function.BiFunction;

import static com.nessxxiii.banksys.utils.Formatter.formatAmount;
import static com.nessxxiii.banksys.utils.Formatter.formatBalance;
import static com.nessxxiii.banksys.utils.ResponseHandler.*;

public class EconomyService {

    // Method for processing transactions in the economy plugin and bank system (database)
    // Side effect: May update player balance in the economy plugin and log the transaction
    public static String processEconomyTransactionAndLog(Economy economy, TransactionLogger transactionLogger, OfflinePlayer player, int amount, TransactionType transactionType, BiFunction<OfflinePlayer, Integer, EconomyResponse> transactionFunc, UUID playerUUID, double oldEssentialsBal, int oldBankBal, int newBankBal) {
        // Process transaction in the economy plugin
        EconomyResponse response = transactionFunc.apply(player, amount);

        // Check if the economy transaction was successful
        if (!response.transactionSuccess()) {
            // Log transaction as unsuccessful
            transactionLogger.logTransaction(playerUUID, amount, oldBankBal, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E3);
            return ERROR_MESSAGE + TransactionStatus.ERROR_E3;
        }
        // Log transaction as successful
        transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, response.balance, transactionType, TransactionStatus.SUCCESS);
        return TRANSACTION_SUCCESS_MESSAGE + transactionType + " of " + formatAmount(amount) + ChatColor.GREEN + "\n"
                + WALLET_BALANCE + formatBalance(response.balance) + ChatColor.LIGHT_PURPLE + "\n"
                + BANK_BALANCE_MESSAGE + formatBalance(newBankBal);
    }

}
