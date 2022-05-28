package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.DataBase;
import com.nessxxiii.banksys4.models.TransactionLog;
import com.nessxxiii.banksys4.models.TransactionStatus;
import com.nessxxiii.banksys4.models.TransactionType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ATM {
    private final DataBase database;
    private final Economy economy;

    public ATM(Banksys4 plugin) {
        this.database = plugin.getDatabase();
        this.economy = plugin.getEconomy();
    }

    public void getBalance(Player player) {
        String name = player.getName();
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();

        try {
            database.createPlayerBalanceIfNotExists(UUID);
            Integer balance = database.findPlayerBalance(UUID);
            player.sendMessage(ChatColor.GREEN + "BankBalance: " + ChatColor.YELLOW + balance);
            new TransactionLog(name, balance, TransactionType.INQUIRY, TransactionStatus.SUCCESS).print();
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Unable to retrieve balance, please let an administrator know: " + TransactionStatus.ERROR_E5);
            new TransactionLog(name, 0, TransactionType.INQUIRY, TransactionStatus.ERROR_E5).print();
            e.printStackTrace();
        }
    }

    public void withdraw(Player player, int amount) {
        String name = player.getName();
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();

        Integer oldBankBal;
        Integer newBankBal;
        Double oldEssentialsBal;
        Double newEssentialsBal;

        try {
            // Fetch bank balance
            database.createPlayerBalanceIfNotExists(UUID);
            oldBankBal = database.findPlayerBalance(UUID);

            // Validate that player has sufficient funds
            if (amount > oldBankBal) {
                player.sendMessage(ChatColor.RED + "Insufficient Funds.");
                player.sendMessage(ChatColor.GREEN + "Amount requested: " + ChatColor.YELLOW + amount);
                player.sendMessage(ChatColor.GREEN + "Bank balance: " + ChatColor.RED + oldBankBal);
                new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.INSUFFICIENT_FUNDS).print();
                return;
            }

            // Remove amount from players bank
            database.updatePlayerBalance(UUID, -amount);
            newBankBal = database.findPlayerBalance(UUID);
        } catch (Exception ex) {
            // Database update failed - notify the player and print a log
            // Neither the players balance nor the database should have changed.
            new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.ERROR_E1).print();
            ex.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error withdrawing money, please let an administrator know: " + TransactionStatus.ERROR_E1);
            return;
        }

        // Add amount to players balance
        oldEssentialsBal = economy.getBalance(player);
        EconomyResponse response = economy.depositPlayer(player, amount);
        newEssentialsBal = economy.getBalance(player);

        if (response.transactionSuccess()) {
            // Transaction successful, send confirmation to player and print a log
            player.sendMessage(ChatColor.GREEN + "Withdraw amount: " + ChatColor.RED + amount);
            player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
            player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + newBankBal);

            TransactionLog log = new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.SUCCESS);
            log.setOldBankBal(oldBankBal);
            log.setNewBankBal(newBankBal);
            log.setOldEssentialsBal(oldEssentialsBal.intValue());
            log.setNewEssentialsBal(newEssentialsBal.intValue());
            log.print();
        } else {
            // Transaction failed, send message to player and print a log.
            // We withdrew the amount from the players balance, but they never received the money.
            // This will require manual review and a refund.
            player.sendMessage(ChatColor.RED + "There was an error withdrawing money, please let an administrator know: " + TransactionStatus.ERROR_E2);
            TransactionLog log = new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.ERROR_E2);
            log.setOldBankBal(oldBankBal);
            log.setNewBankBal(newBankBal);
            log.setOldEssentialsBal(oldEssentialsBal.intValue());
            log.setNewEssentialsBal(newEssentialsBal.intValue());
            log.print();
        }
    }

    public void deposit(Player player, int amount) {
        String name = player.getName();
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();

        Integer oldBankBal = null;
        Integer newBankBal = null;
        Double oldEssentialsBal = economy.getBalance(player);
        Double newEssentialsBal = null;

        // Validate that player has sufficient funds
        if (amount > oldEssentialsBal) {
            player.sendMessage(ChatColor.RED + "Insufficient Funds.");
            player.sendMessage(ChatColor.GREEN + "Amount requested: " + ChatColor.YELLOW + amount);
            player.sendMessage(ChatColor.GREEN + "Pocket balance: " + ChatColor.RED + oldEssentialsBal.intValue());
            new TransactionLog(name, amount, TransactionType.DEPOSIT, TransactionStatus.INSUFFICIENT_FUNDS).print();
            return;
        }

        // Remove the amount from the players balance
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        newEssentialsBal = economy.getBalance(player);

        if (response.transactionSuccess()) {
            try {
                // Add the balance to the players bank
                database.createPlayerBalanceIfNotExists(UUID);
                oldBankBal = database.findPlayerBalance(UUID);
                database.updatePlayerBalance(UUID, amount);
                newBankBal = database.findPlayerBalance(UUID);

                // Transaction was successful - send confirmation to player and print a log
                player.sendMessage(ChatColor.GREEN + "Withdraw amount: " + ChatColor.RED + amount);
                player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
                player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + newBankBal);

                TransactionLog log = new TransactionLog(name, amount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS);
                log.setOldBankBal(oldBankBal);
                log.setNewBankBal(newBankBal);
                log.setOldEssentialsBal(oldEssentialsBal.intValue());
                log.setNewEssentialsBal(newEssentialsBal.intValue());
                log.print();
            } catch (Exception ex) {
                // Bank transaction failed - send message to player and print a log.
                // The bank balance should not have been modified.
                // The player balance was modified, and an automated refund is issued - this should be verified manually
                player.sendMessage(ChatColor.RED + "There was an error depositing money, please let an administrator know: " + TransactionStatus.ERROR_E3);
                economy.depositPlayer(player, amount);
                newEssentialsBal = economy.getBalance(player);

                TransactionLog log = new TransactionLog(name, amount, TransactionType.DEPOSIT, TransactionStatus.ERROR_E3);
                log.setOldBankBal(oldBankBal);
                log.setNewBankBal(newBankBal);
                log.setOldEssentialsBal(oldEssentialsBal.intValue());
                log.setNewEssentialsBal(newEssentialsBal.intValue());
                log.print();
                ex.printStackTrace();
            }
        } else {
            // Economy transaction failed - send message to player a print a log
            // Neither the players balance nor bank balance should have been modified.
            player.sendMessage(ChatColor.RED + "There was an error depositing money, please let an administrator know: " + TransactionStatus.ERROR_E4);
            TransactionLog log = new TransactionLog(name, amount, TransactionType.DEPOSIT, TransactionStatus.ERROR_E4);
            log.setOldBankBal(oldBankBal);
            log.setNewBankBal(newBankBal);
            log.setOldEssentialsBal(oldEssentialsBal.intValue());
            log.setNewEssentialsBal(newEssentialsBal.intValue());
            log.print();
        }
    }
}
