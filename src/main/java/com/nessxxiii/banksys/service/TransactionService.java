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
                return playerBalanceDAO.findPlayerBalance(playerUUID).get().toString();
            } else {
                throw new DatabaseOperationException("No player found");
            }
        } catch (SQLException | DatabaseOperationException e) {
            Bukkit.getConsoleSender().sendMessage("BankSys: "+ e.getMessage());
            return "No bank balance exists.";
        }
    }

    public void deposit(OfflinePlayer player, String arg) {
        processTransaction(player, arg, TransactionType.DEPOSIT, economy::withdrawPlayer);
    }

    public void withdraw(OfflinePlayer player, String arg) {
        processTransaction(player, arg, TransactionType.WITHDRAW, economy::depositPlayer);
    }

    private void processTransaction(OfflinePlayer player, String arg, TransactionType transactionType, BiFunction<OfflinePlayer, Integer, EconomyResponse> transactionFunc) {
        UUID playerUUID = player.getUniqueId();

        // Get current balance and amount to transfer
        double oldEssentialsBal = economy.getBalance(player);
        Integer amount;
        try {
            amount = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            customLogger.sendLog("Player did not provide an integer value.");
            ex.printStackTrace();
            return;
        }

        // Check if player has sufficient funds
        if (transactionType == TransactionType.DEPOSIT && amount > oldEssentialsBal) {
            transactionLogger.logTransaction(playerUUID, amount, transactionType, TransactionStatus.INSUFFICIENT_FUNDS);
            return;
        }

        try {
            // Create the balance if not exists
            playerBalanceDAO.createPlayerBalanceIfNotExists(playerUUID);

            // Get old bank balance
            Integer oldBankBal = playerBalanceDAO.findPlayerBalance(playerUUID).orElseThrow(() -> new SQLException("Old player balance for player " + player.getName() + " was not found!"));

            // Process the transaction
            EconomyResponse response = transactionFunc.apply(player, amount);
            if (!response.transactionSuccess()) {
                transactionLogger.logTransaction(playerUUID, amount, oldBankBal, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E3);
                return;
            }

            // Get new bank balance
            Integer newBankBal = playerBalanceDAO.findPlayerBalance(playerUUID).get();

            // Log the transaction
            transactionLogger.logTransaction(playerUUID, amount, oldBankBal, newBankBal, oldEssentialsBal, response.balance, transactionType, TransactionStatus.SUCCESS);

        } catch (SQLException ex) {
            transactionLogger.logTransaction(playerUUID, amount, null, null, oldEssentialsBal, economy.getBalance(player), transactionType, TransactionStatus.ERROR_E4);
            ex.printStackTrace();
        }
    }
}
