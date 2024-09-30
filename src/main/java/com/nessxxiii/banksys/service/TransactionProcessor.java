package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import com.nessxxiii.banksys.logging.TransactionLogger;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.Response;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.function.BiFunction;

import static com.nessxxiii.banksys.service.BankService.processBankTransaction;
import static com.nessxxiii.banksys.service.EconomyService.processEconomyTransactionAndLog;
import static com.nessxxiii.banksys.utils.ResponseHandler.buildErrorMessage;
import static com.nessxxiii.banksys.utils.ResponseHandler.buildInsufficientFundsMessage;

public class TransactionProcessor {
    private final Economy economy;
    private final PlayerBalanceDAO playerBalanceDAO;
    private final CustomLogger customLogger;

    public TransactionProcessor(Economy economy, PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger) {
        this.economy = economy;
        this.playerBalanceDAO = playerBalanceDAO;
        this.customLogger = customLogger;
    }
    // Method for processing transactions
    // This method fetches player balance (from economy and DAO), checks if transaction is possible, and attempts to process it
    // Side effects: May update player balance in both economy plugin and database, and log the transaction
    public String processTransaction(OfflinePlayer player, int amount, TransactionType transactionType, BiFunction<OfflinePlayer, Integer, EconomyResponse> transactionFunc) {
        UUID playerUUID = player.getUniqueId();
        // Fetch the current balance from economy plugin
        double oldEssentialsBal = economy.getBalance(player);
        // Check if player has sufficient funds in economy plugin for the transaction
        if (!hasSufficientFunds(transactionType, amount, oldEssentialsBal, playerUUID)) {
            return buildInsufficientFundsMessage(amount, oldEssentialsBal);
        }

        // Fetch the current balance from the database
        Response<Integer> oldBankBalResponse = playerBalanceDAO.findPlayerBalance(playerUUID);
        if (oldBankBalResponse.error() != null) {
            return buildErrorMessage(amount, 0);
        }
        // Try to process the transaction in the bank system (database)
        Response<Integer> newBankBalResponse = processBankTransaction(playerBalanceDAO, customLogger, playerUUID, amount, transactionType, oldBankBalResponse.value());
        // Check if bank transaction was successful
        if (newBankBalResponse.error() != null) {
            return buildErrorMessage(amount, oldBankBalResponse.value());
        }
        // If bank transaction was successful, process the transaction in the economy plugin
        return processEconomyTransactionAndLog(economy, player, amount, transactionType, transactionFunc, playerUUID, oldEssentialsBal, oldBankBalResponse.value(), newBankBalResponse.value());
    }

    // Check if player has sufficient funds in the economy plugin
    private boolean hasSufficientFunds(TransactionType transactionType, int amount, double oldEssentialsBal, UUID playerUUID) {
        if (transactionType == TransactionType.DEPOSIT && amount > oldEssentialsBal) {
            TransactionLogger.logTransaction(playerUUID, amount, transactionType, TransactionStatus.INSUFFICIENT_FUNDS);
            return false;
        }
        return true;
    }
}

