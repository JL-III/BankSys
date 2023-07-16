package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import com.nessxxiii.banksys.logging.TransactionLogger;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import static com.nessxxiii.banksys.service.BankService.processBankTransaction;
import static com.nessxxiii.banksys.service.EconomyService.processEconomyTransactionAndLog;
import static com.nessxxiii.banksys.utils.ResponseHandler.*;

public class TransactionProcessor {
    private final Economy economy;
    private final TransactionLogger transactionLogger;
    private final PlayerBalanceDAO playerBalanceDAO;
    private final CustomLogger customLogger;

    public TransactionProcessor(Economy economy, TransactionLogger transactionLogger, PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger) {
        this.economy = economy;
        this.transactionLogger = transactionLogger;
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

        try {
            // Fetch the current balance from the database
            Optional<Integer> oldBankBal = playerBalanceDAO.findPlayerBalance(playerUUID);
            if (oldBankBal.isEmpty()) {
                return buildErrorMessage(amount, 0);
            }
            // Try to process the transaction in the bank system (database)
            Optional<Integer> newBankBalOpt = processBankTransaction(playerBalanceDAO, customLogger, playerUUID, amount, transactionType, oldBankBal.get());
            // Check if bank transaction was successful
            if (newBankBalOpt.isEmpty()) {
                return buildErrorMessage(amount, oldBankBal.get());
            }
            // If bank transaction was successful, process the transaction in the economy plugin
            return processEconomyTransactionAndLog(economy, transactionLogger, player, amount, transactionType, transactionFunc, playerUUID, oldEssentialsBal, oldBankBal.get(), newBankBalOpt.get());
        } catch (SQLException ex) {
            // Log transaction as unsuccessful
            transactionLogger.logTransaction(playerUUID, amount, null, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E4);
            ex.printStackTrace();
            return ERROR_MESSAGE + TransactionStatus.ERROR_E4;
        }
    }

    // Check if player has sufficient funds in the economy plugin
    private boolean hasSufficientFunds(TransactionType transactionType, int amount, double oldEssentialsBal, UUID playerUUID) {
        if (transactionType == TransactionType.DEPOSIT && amount > oldEssentialsBal) {
            transactionLogger.logTransaction(playerUUID, amount, transactionType, TransactionStatus.INSUFFICIENT_FUNDS);
            return false;
        }
        return true;
    }

}

