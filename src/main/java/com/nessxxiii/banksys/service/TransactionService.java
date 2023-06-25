package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import com.nessxxiii.banksys.exceptions.DatabaseOperationException;
import com.nessxxiii.banksys.util.TransactionLogger;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

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
                return playerBalanceDAO.findPlayerBalance(playerUUID).get().toString();
            } else {
                throw new DatabaseOperationException("No player found");
            }
        } catch (SQLException | DatabaseOperationException e) {
            Bukkit.getConsoleSender().sendMessage("BankSys: "+ e.getMessage());
            return "No bank balance exists.";
        }
    }

    public void withdraw(OfflinePlayer player, String arg) {
        UUID playerUUID = player.getUniqueId();
        Integer amount;
        Integer oldBankBal;
        Integer newBankBal;
        double oldEssentialsBal;

        try {
            oldEssentialsBal = economy.getBalance(player);
            amount = Integer.parseInt(arg);  // Should catch NumberFormatException specifically here for better error handling.
        } catch (NumberFormatException ex){
            customLogger.sendLog("Player did not provide an integer value.");
            ex.printStackTrace();
            return;
        }

        try {
            playerBalanceDAO.createPlayerBalanceIfNotExists(playerUUID);
            Optional<Integer> oldBankBalOpt = playerBalanceDAO.findPlayerBalance(playerUUID);

            // Validate that player has a balance
            if (oldBankBalOpt.isEmpty()) {
                transactionLogger.logTransaction(playerUUID, amount, TransactionType.WITHDRAW, TransactionStatus.INSUFFICIENT_FUNDS);
                return;
            }

            oldBankBal = oldBankBalOpt.get();

            // Validate that player has sufficient funds
            if (amount > oldBankBal) {
                transactionLogger.logTransaction(playerUUID, amount, TransactionType.WITHDRAW, TransactionStatus.INSUFFICIENT_FUNDS);
                return;
            }

            // Remove amount from players bank
            playerBalanceDAO.updatePlayerBalance(playerUUID, -amount);
            Optional<Integer> newBankBalOpt = playerBalanceDAO.findPlayerBalance(playerUUID);

            // Check if balance updated successfully
            if (newBankBalOpt.isEmpty()) {  // Should handle this error with proper exception or error message.
                return;
            }

            newBankBal = newBankBalOpt.get();
        } catch (Exception ex) {  // SQLException should be caught here, as it is more specific and will give more information.
            transactionLogger.logTransaction(playerUUID, amount, TransactionType.WITHDRAW, TransactionStatus.ERROR_E1);
            ex.printStackTrace();
            return;
        }

        // Add amount to players balance
        EconomyResponse response = economy.depositPlayer(player, amount);

        if (response.transactionSuccess()) {
            // Transaction successful
            transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, response.balance, TransactionType.WITHDRAW, TransactionStatus.SUCCESS);
        } else {
            // Transaction failed
            // We withdrew the amount from the players balance, but they never received the money.
            // This will require manual review and a refund.
            transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, response.balance, TransactionType.WITHDRAW, TransactionStatus.ERROR_E2);
            // Refund to player's bank account should be initiated here to keep data consistency.
        }
    }

    public void deposit(OfflinePlayer player, String arg) {
        UUID playerUUID = player.getUniqueId();

        Integer oldBankBal = null;
        Integer newBankBal = null;
        double oldEssentialsBal = economy.getBalance(player);
        double newEssentialsBal;
        Integer amount;
        try {
            amount = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            customLogger.sendLog("Player did not provide an integer value.");
            ex.printStackTrace();
            return;
        }

        // Validate that player has sufficient funds
        if (amount > oldEssentialsBal) {
            transactionLogger.logTransaction(playerUUID, amount, TransactionType.DEPOSIT, TransactionStatus.INSUFFICIENT_FUNDS);
            return;
        }

        // Remove the amount from the players balance
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        newEssentialsBal = economy.getBalance(player);

        if (response.transactionSuccess()) {
            try {
                // Add the balance to the players bank
                playerBalanceDAO.createPlayerBalanceIfNotExists(playerUUID);
                Optional<Integer> oldBankBalOpt = playerBalanceDAO.findPlayerBalance(playerUUID);

                if (oldBankBalOpt.isPresent()) {
                    oldBankBal = oldBankBalOpt.get();
                } else {
                    throw new SQLException("Old player balance for player " + player.getName() + " was not found!");
                }

                playerBalanceDAO.updatePlayerBalance(playerUUID, amount);
                // Transaction was successful
                transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.DEPOSIT, TransactionStatus.SUCCESS);
            } catch (Exception ex) {
                // Bank transaction failed - send message to player and print a log.
                // The bank balance should not have been modified.
                // The player balance was modified, and an automated refund is issued - this should be verified manually
                economy.depositPlayer(player, amount);
                newEssentialsBal = economy.getBalance(player);

                transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.DEPOSIT, TransactionStatus.ERROR_E3);
                ex.printStackTrace();
            }
        } else {
            // Economy transaction failed
            // Neither the players balance nor bank balance should have been modified.
            transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.DEPOSIT, TransactionStatus.ERROR_E4);
        }
    }
}
