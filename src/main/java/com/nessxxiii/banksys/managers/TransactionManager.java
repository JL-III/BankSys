package com.nessxxiii.banksys.managers;

import com.nessxxiii.banksys.db.PlayerBalanceDAO;
import com.nessxxiii.banksys.data.TransactionLog;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import com.nessxxiii.banksys.util.TransactionLogger;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.swing.text.html.Option;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class TransactionManager {
    private final PlayerBalanceDAO playerBalanceDAO;
    private final Economy economy;

    private final CustomLogger customLogger;

    private final TransactionLogger transactionLogger = new TransactionLogger();

    public TransactionManager(Economy economy, PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger) {
        this.economy = economy;
        this.playerBalanceDAO = playerBalanceDAO;
        this.customLogger = customLogger;
    }

    //For internal usage
    public Integer getBankBalance(Player player) {
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();

        try {
            playerBalanceDAO.createPlayerBalanceIfNotExists(UUID);
            return playerBalanceDAO.findPlayerBalance(UUID).get();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    //Used for the balance command
    public String inquireBankBalance(OfflinePlayer player) {
        String UUID = player.getUniqueId().toString();
        try {
            if (playerBalanceDAO.findPlayerBalance(UUID).isPresent()) {
                return playerBalanceDAO.findPlayerBalance(UUID).toString();
            } else {
                throw new Exception("No player found");
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("BankSys: "+ e.getMessage());
            return "No bank balance exists.";
        }
    }

    public void withdrawFromBank(OfflinePlayer player, int amount) {
        String name = player.getName();
        String playerUUID = player.getUniqueId().toString();

        Integer oldBankBal;
        Integer newBankBal;
        double oldEssentialsBal;
        double newEssentialsBal;

        //check if player has a balance on the Main server before continuing to attempt to affect the database balance
        try {
            oldEssentialsBal = economy.getBalance(player);
            newEssentialsBal = economy.getBalance(player);

        } catch (Exception ex){
            customLogger.sendLog("Found bank balance that belongs to a player that does not have an account on the Main Server");
            customLogger.sendLog("Bank will attempt to transfer this players balance again tomorrow");
            return;
        }


        try {
            // Fetch bank balance
            playerBalanceDAO.createPlayerBalanceIfNotExists(playerUUID);
            Optional<Integer> oldBankBalOpt = playerBalanceDAO.findPlayerBalance(playerUUID);

            // Validate that player has a balance
            if (!oldBankBalOpt.isPresent()) {
                transactionLogger.logTransaction(name, amount, TransactionType.WITHDRAW, TransactionStatus.INSUFFICIENT_FUNDS);
                return;
            }

            oldBankBal = oldBankBalOpt.get();

            // Validate that player has sufficient funds
            if (amount > oldBankBal) {
                transactionLogger.logTransaction(name, amount, TransactionType.WITHDRAW, TransactionStatus.INSUFFICIENT_FUNDS);
                return;
            }

            // Remove amount from players bank
            playerBalanceDAO.updatePlayerBalance(playerUUID, -amount);
            Optional<Integer> newBankBalOpt = playerBalanceDAO.findPlayerBalance(playerUUID);

            // Check if balance updated successfully
            if (!newBankBalOpt.isPresent()) {
                // Handle the error appropriately. For example, throw an exception or log an error.
                return;
            }

            newBankBal = newBankBalOpt.get();
        } catch (Exception ex) {
            // Database update failed - notify the player and print a log
            // Neither the players balance nor the database should have changed.
            transactionLogger.logTransaction(name, amount, TransactionType.WITHDRAW, TransactionStatus.ERROR_E1);
            ex.printStackTrace();
            return;
        }

        // Add amount to players balance
        EconomyResponse response = economy.depositPlayer(player, amount);

        if (response.transactionSuccess()) {
            // Transaction successful
            transactionLogger.logTransaction(name, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.WITHDRAW, TransactionStatus.SUCCESS);
        } else {
            // Transaction failed
            // We withdrew the amount from the players balance, but they never received the money.
            // This will require manual review and a refund.
            transactionLogger.logTransaction(name, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.WITHDRAW, TransactionStatus.ERROR_E2);
        }
    }

    public void depositToBank(OfflinePlayer player, int amount) {
        String name = player.getName();
        String playerUUID = player.getUniqueId().toString();

        Integer oldBankBal = null;
        Integer newBankBal = null;
        double oldEssentialsBal = economy.getBalance(player);
        double newEssentialsBal;

        // Validate that player has sufficient funds
        if (amount > oldEssentialsBal) {
            transactionLogger.logTransaction(name, amount, TransactionType.DEPOSIT, TransactionStatus.INSUFFICIENT_FUNDS);
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
                Optional<Integer> newBankBalOpt = playerBalanceDAO.findPlayerBalance(playerUUID);
                if (oldBankBalOpt.isPresent()) {
                    oldBankBal = oldBankBalOpt.get();
                } else {
                    throw new SQLException("Old player balance for player " + name + " was not found!");
                }

                if (newBankBalOpt.isPresent()) {
                    newBankBal = newBankBalOpt.get();
                } else {
                    throw new SQLException("New player balance for player " + name + " was not found!");
                }

                playerBalanceDAO.updatePlayerBalance(playerUUID, amount);
                // Transaction was successful
                transactionLogger.logTransaction(name, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.DEPOSIT, TransactionStatus.SUCCESS);
            } catch (Exception ex) {
                // Bank transaction failed - send message to player and print a log.
                // The bank balance should not have been modified.
                // The player balance was modified, and an automated refund is issued - this should be verified manually
                economy.depositPlayer(player, amount);
                newEssentialsBal = economy.getBalance(player);

                transactionLogger.logTransaction(name, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.DEPOSIT, TransactionStatus.ERROR_E3);
                ex.printStackTrace();
            }
        } else {
            // Economy transaction failed
            // Neither the players balance nor bank balance should have been modified.
            transactionLogger.logTransaction(name, amount, oldBankBal, newBankBal, oldEssentialsBal, newEssentialsBal, TransactionType.DEPOSIT, TransactionStatus.ERROR_E4);
        }
    }
}
