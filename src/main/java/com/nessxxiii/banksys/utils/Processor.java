package com.nessxxiii.banksys.utils;

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

import static com.nessxxiii.banksys.utils.Formatter.formatAmount;
import static com.nessxxiii.banksys.utils.Formatter.formatBalance;

public class Processor {
    private static final String INSUFFICIENT_FUNDS_MESSAGE = ChatColor.RED + "You do not have sufficient funds!\nDeposit Requested: ";
    private static final String WALLET_BALANCE_MESSAGE = ChatColor.RED + "Wallet: ";
    private static final String TRANSACTION_SUCCESS_MESSAGE = ChatColor.GREEN + "Successful ";
    private static final String BALANCE_MESSAGE = "Balance: ";
    private static final String BANK_BALANCE_MESSAGE = "Bank Balance: ";
    private static final String ERROR_MESSAGE = ChatColor.RED + "There was an error processing this command - Contact an admin immediately for help!\n";
    private final Economy economy;
    private final TransactionLogger transactionLogger;
    private final PlayerBalanceDAO playerBalanceDAO;
    private final CustomLogger customLogger;

    public Processor(Economy economy, TransactionLogger transactionLogger, PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger) {
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
            Optional<Integer> newBankBalOpt = processBankTransaction(playerUUID, amount, transactionType, oldBankBal.get());

            // Check if bank transaction was successful
            if (newBankBalOpt.isEmpty()) {
                return buildErrorMessage(amount, oldBankBal.get());
            }

            // If bank transaction was successful, process the transaction in the economy plugin
            return processEconomyTransactionAndLog(player, amount, transactionType, transactionFunc, playerUUID, oldEssentialsBal, oldBankBal.get(), newBankBalOpt.get());

        } catch (SQLException ex) {
            // Log transaction as unsuccessful
            transactionLogger.logTransaction(playerUUID, amount, null, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E4);
            ex.printStackTrace();
            return ERROR_MESSAGE + TransactionStatus.ERROR_E4;
        }
    }

    // Method for processing transactions in the bank system (database)
    // Side effect: May update player balance in the database and log the transaction
    private Optional<Integer> processBankTransaction(UUID playerUUID, int amount, TransactionType transactionType, int oldBankBalOpt) {
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
            return Optional.empty();
        }
    }


    // Method for processing transactions in the economy plugin and bank system (database)
    // Side effect: May update player balance in the economy plugin and database, and log the transaction
    private String processEconomyTransactionAndLog(OfflinePlayer player, int amount, TransactionType transactionType, BiFunction<OfflinePlayer, Integer, EconomyResponse> transactionFunc, UUID playerUUID, double oldEssentialsBal, int oldBankBal, int newBankBal) {
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
                + BALANCE_MESSAGE + formatBalance(response.balance) + ChatColor.LIGHT_PURPLE + "\n"
                + BANK_BALANCE_MESSAGE + formatBalance(newBankBal);
    }

    // Check if player has sufficient funds in the economy plugin
// Side effect: May log the transaction
    private boolean hasSufficientFunds(TransactionType transactionType, int amount, double oldEssentialsBal, UUID playerUUID) {
        if (transactionType == TransactionType.DEPOSIT && amount > oldEssentialsBal) {
            transactionLogger.logTransaction(playerUUID, amount, transactionType, TransactionStatus.INSUFFICIENT_FUNDS);
            return false;
        }
        return true;
    }

    // Generate a message for insufficient funds
    private String buildInsufficientFundsMessage(int amount, double oldEssentialsBal) {
        return INSUFFICIENT_FUNDS_MESSAGE
                + formatAmount(amount) + "\n"
                + WALLET_BALANCE_MESSAGE
                + formatBalance(oldEssentialsBal);
    }

    // Generate an error message
    private String buildErrorMessage(int amount, int oldBankBal) {
        return ChatColor.RED
                + "An error occurred, do you have sufficient funds?" + ChatColor.YELLOW + "\n"
                + "Requested Amount: " + formatAmount(amount) + ChatColor.LIGHT_PURPLE + "\n"
                + BANK_BALANCE_MESSAGE + formatBalance(oldBankBal);
    }

}

