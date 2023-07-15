package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import com.nessxxiii.banksys.exceptions.DatabaseOperationException;
import com.nessxxiii.banksys.logging.TransactionLogger;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiFunction;

public class TransactionService {
    private final PlayerBalanceDAO playerBalanceDAO;
    private final Economy economy;
    private final CustomLogger customLogger;
    private final TransactionLogger transactionLogger = new TransactionLogger();

    public TransactionService(Economy economy, PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger) {
        this.economy = economy;
        this.playerBalanceDAO = playerBalanceDAO;
        this.customLogger = customLogger;
    }

    //Used for the balance command
    public String inquiry(UUID playerUUID) {
        try {
            if (playerBalanceDAO.findPlayerBalance(playerUUID).isPresent()) {
                return formatter().format(playerBalanceDAO.findPlayerBalance(playerUUID).get());
            } else {
                throw new DatabaseOperationException("No player found");
            }
        } catch (SQLException | DatabaseOperationException e) {
            Bukkit.getConsoleSender().sendMessage("BankSys: "+ e.getMessage());
            return "No bank balance exists.";
        }
    }

    public String deposit(OfflinePlayer player, int amount) {
       return processTransaction(player, amount, TransactionType.DEPOSIT, economy::withdrawPlayer);
    }

    public String withdraw(OfflinePlayer player, int amount) {
       return processTransaction(player, amount, TransactionType.WITHDRAWAL, economy::depositPlayer);
    }

    private String processTransaction(OfflinePlayer player, int amount, TransactionType transactionType, BiFunction<OfflinePlayer, Integer, EconomyResponse> transactionFunc) {
        UUID playerUUID = player.getUniqueId();

        // Get current balance and amount to transfer
        double oldEssentialsBal = economy.getBalance(player);

        // Check if player has sufficient funds
        if (transactionType == TransactionType.DEPOSIT && amount > oldEssentialsBal) {
            transactionLogger.logTransaction(playerUUID, amount, transactionType, TransactionStatus.INSUFFICIENT_FUNDS);
            return ChatColor.RED
                    + "You do not have sufficient funds!\n"
                    + "Deposit Requested: " + ChatColor.YELLOW + formatter().format(amount) + "\n"
                    + ChatColor.RED
                    + "Wallet: " + ChatColor.YELLOW + formatter().format(oldEssentialsBal);
        }

        try {
            // Get old bank balance
            Integer oldBankBal = playerBalanceDAO.findPlayerBalance(playerUUID).orElseThrow(() -> new SQLException("Old player balance for player " + player.getName() + " was not found!"));
            // Process the transaction on bank system
            int newBankBal = processBankTransaction(playerUUID, amount, transactionType);
            // If bank transaction successful, process the transaction on economy plugin
            if (newBankBal >= 0) {
                EconomyResponse response = transactionFunc.apply(player, amount);
                if (!response.transactionSuccess()) {
                    transactionLogger.logTransaction(playerUUID, amount, oldBankBal, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E3);
                    return ChatColor.RED
                            + "There was an error processing this command - Contact an admin immediately for help!\n"
                            + TransactionStatus.ERROR_E3;
                }
                // Log the transaction
                transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, response.balance, transactionType, TransactionStatus.SUCCESS);
                return ChatColor.GREEN
                        + "Successful " + transactionType + " of " + ChatColor.YELLOW + formatter().format(amount) + ChatColor.GREEN + "\n"
                        + "Balance: " + ChatColor.RED + formatter().format(response.balance) + ChatColor.LIGHT_PURPLE + "\n"
                        + "Bank Balance: " + ChatColor.YELLOW + formatter().format(newBankBal);
            }
            return ChatColor.RED
                    + "An error occurred, do you have sufficient funds?" + ChatColor.YELLOW + "\n"
                    + "Requested Amount: " + ChatColor.RED + formatter().format(amount) + ChatColor.LIGHT_PURPLE + "\n"
                    + "Bank Balance: " + ChatColor.YELLOW + formatter().format(oldBankBal);
        } catch (SQLException ex) {
            transactionLogger.logTransaction(playerUUID, amount, null, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E4);
            ex.printStackTrace();
            return ChatColor.RED
                    + "There was an error processing this command - Contact an admin immediately for help!\n"
                    + TransactionStatus.ERROR_E4;
        }
    }

    // New method to process transactions on the bank system
    private int processBankTransaction(UUID playerUUID, int amount, TransactionType transactionType) {
        try {
            switch (transactionType) {
                case DEPOSIT -> {
                    return playerBalanceDAO.updatePlayerBalance(playerUUID, amount);
                }
                case WITHDRAWAL -> {
                    Integer currentBalance = playerBalanceDAO.findPlayerBalance(playerUUID).orElse(0);
                    if (currentBalance < amount) {
                        customLogger.sendLog("Player does not have sufficient bank balance to withdraw " + amount);
                        return -1;
                    }
                    return playerBalanceDAO.updatePlayerBalance(playerUUID, -amount);
                }
                default -> {
                    return -1;
                }
            }
        } catch (SQLException ex) {
            customLogger.sendLog("Failed to update bank balance for player " + playerUUID + " during " + transactionType);
            return -1;
        }
    }

    private NumberFormat formatter() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter;
    }

}
