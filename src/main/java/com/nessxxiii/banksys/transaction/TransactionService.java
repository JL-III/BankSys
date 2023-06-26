package com.nessxxiii.banksys.transaction;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.exceptions.DatabaseOperationException;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

    //For internal usage
    public Integer getBankBalance(Player player) {
        UUID playerUUID = player.getUniqueId();

        try {
            playerBalanceDAO.createPlayerBalanceIfNotExists(playerUUID);
            return playerBalanceDAO.findPlayerBalance(playerUUID).orElseThrow();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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

    public String deposit(OfflinePlayer player, String arg) {
       return processTransaction(player, arg, TransactionType.DEPOSIT, economy::withdrawPlayer);
    }

    public String withdraw(OfflinePlayer player, String arg) {
       return processTransaction(player, arg, TransactionType.WITHDRAWAL, economy::depositPlayer);
    }

    private String processTransaction(OfflinePlayer player, String arg, TransactionType transactionType, BiFunction<OfflinePlayer, Integer, EconomyResponse> transactionFunc) {
        UUID playerUUID = player.getUniqueId();

        // Get current balance and amount to transfer
        double oldEssentialsBal = economy.getBalance(player);
        Integer amount;
        try {
            amount = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            customLogger.sendLog("Player did not provide an integer value.");
            return "Please provide an integer value.";
        }

        // Check if player has sufficient funds
        if (transactionType == TransactionType.DEPOSIT && amount > oldEssentialsBal) {
            transactionLogger.logTransaction(playerUUID, amount, transactionType, TransactionStatus.INSUFFICIENT_FUNDS);
            return ChatColor.RED + "You do not have sufficient funds!\n" + "Deposit Requested: " + amount + "\n" + "Balance: " + oldEssentialsBal;
        }

        try {
            // Create the balance if not exists
            playerBalanceDAO.createPlayerBalanceIfNotExists(playerUUID);

            // Get old bank balance
            Integer oldBankBal = playerBalanceDAO.findPlayerBalance(playerUUID).orElseThrow(() -> new SQLException("Old player balance for player " + player.getName() + " was not found!"));

            // Process the transaction on bank system
            int newBankBal = processBankTransaction(playerUUID, amount, transactionType);

            // If bank transaction successful, process the transaction on economy plugin
            if (newBankBal >= 0) {
                EconomyResponse response = transactionFunc.apply(player, amount);
                if (!response.transactionSuccess()) {
                    transactionLogger.logTransaction(playerUUID, amount, oldBankBal, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E3);
                    return ChatColor.RED + "There was an error processing this command - Contact an admin immediately for help!\n" + TransactionStatus.ERROR_E3;
                }

                // Log the transaction
                transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, response.balance, transactionType, TransactionStatus.SUCCESS);
                return ChatColor.GREEN + "Successful " + transactionType + " of " + ChatColor.YELLOW + formatter().format(amount) + ChatColor.GREEN + "\nBalance: " + ChatColor.RED + formatter().format(response.balance) + ChatColor.LIGHT_PURPLE + "\nBank Balance: " + ChatColor.YELLOW + formatter().format(newBankBal);
            }
            return ChatColor.RED + "An error occurred, do you have sufficient funds?" + ChatColor.YELLOW + "\nRequested Amount: " + ChatColor.RED + formatter().format(amount) + ChatColor.LIGHT_PURPLE + "\nBank Balance: " + ChatColor.YELLOW + formatter().format(oldBankBal);
        } catch (SQLException ex) {
            transactionLogger.logTransaction(playerUUID, amount, null, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E4);
            ex.printStackTrace();
            return ChatColor.RED + "There was an error processing this command - Contact an admin immediately for help!\n" + TransactionStatus.ERROR_E4;
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
