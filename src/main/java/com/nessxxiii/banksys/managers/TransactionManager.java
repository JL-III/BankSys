package com.nessxxiii.banksys.managers;

import com.nessxxiii.banksys.BankSys;
import com.nessxxiii.banksys.db.Bank;
import com.nessxxiii.banksys.models.TransactionLog;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.UUID;

public class TransactionManager {
    private final Bank bank;
    private final Economy economy;

    public TransactionManager(BankSys plugin) {
        this.bank = new Bank(plugin);
        this.economy = plugin.getEconomy();
    }

    //For internal usage
    public Integer getBankBalance(Player player) {
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();

        try {
            bank.createPlayerBalanceIfNotExists(UUID);
            return bank.findPlayerBalance(UUID);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    //Used for the balance command
    public String inquireBankBalance(OfflinePlayer player) {
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();
        try {
            if (bank.findPlayerBalance(UUID) != null) {
                return bank.findPlayerBalance(UUID).toString();
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
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();

        Integer oldBankBal;
        Integer newBankBal;
        Double oldEssentialsBal;
        Double newEssentialsBal;

        //check if player has a balance on the Main server before continuing to attempt to affect the database balance
        try {
            oldEssentialsBal = economy.getBalance(player);
            newEssentialsBal = economy.getBalance(player);

        } catch (Exception ex){
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Found bank balance that belongs to a player that does not have an account on the Main Server");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Bank will attempt to transfer this players balance again tomorrow");
            return;
        }


        try {
            // Fetch bank balance
            bank.createPlayerBalanceIfNotExists(UUID);
            oldBankBal = bank.findPlayerBalance(UUID);

            // Validate that player has sufficient funds
            if (amount > oldBankBal) {
                new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.INSUFFICIENT_FUNDS).print();
                return;
            }

            // Remove amount from players bank
            bank.updatePlayerBalance(UUID, -amount);
            newBankBal = bank.findPlayerBalance(UUID);
        } catch (Exception ex) {
            // Database update failed - notify the player and print a log
            // Neither the players balance nor the database should have changed.
            new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.ERROR_E1).print();
            ex.printStackTrace();
            return;
        }

        // Add amount to players balance
        EconomyResponse response = economy.depositPlayer(player, amount);

        if (response.transactionSuccess()) {
            // Transaction successful
            TransactionLog log = new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.SUCCESS);
            log.setOldBankBal(oldBankBal);
            log.setNewBankBal(newBankBal);
            log.setOldEssentialsBal(oldEssentialsBal.intValue());
            log.setNewEssentialsBal(newEssentialsBal.intValue());
            log.print();
        } else {
            // Transaction failed
            // We withdrew the amount from the players balance, but they never received the money.
            // This will require manual review and a refund.
            TransactionLog log = new TransactionLog(name, amount, TransactionType.WITHDRAW, TransactionStatus.ERROR_E2);
            log.setOldBankBal(oldBankBal);
            log.setNewBankBal(newBankBal);
            log.setOldEssentialsBal(oldEssentialsBal.intValue());
            log.setNewEssentialsBal(newEssentialsBal.intValue());
            log.print();
        }
    }

    public void depositToBank(OfflinePlayer player, int amount) {
        String name = player.getName();
        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();

        Integer oldBankBal = null;
        Integer newBankBal = null;
        Double oldEssentialsBal = economy.getBalance(player);
        Double newEssentialsBal;

        // Validate that player has sufficient funds
        if (amount > oldEssentialsBal) {
            new TransactionLog(name, amount, TransactionType.DEPOSIT, TransactionStatus.INSUFFICIENT_FUNDS).print();
            return;
        }

        // Remove the amount from the players balance
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        newEssentialsBal = economy.getBalance(player);

        if (response.transactionSuccess()) {
            try {
                // Add the balance to the players bank
                bank.createPlayerBalanceIfNotExists(UUID);
                oldBankBal = bank.findPlayerBalance(UUID);
                bank.updatePlayerBalance(UUID, amount);
                newBankBal = bank.findPlayerBalance(UUID);

                // Transaction was successful
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
            // Economy transaction failed
            // Neither the players balance nor bank balance should have been modified.
            TransactionLog log = new TransactionLog(name, amount, TransactionType.DEPOSIT, TransactionStatus.ERROR_E4);
            log.setOldBankBal(oldBankBal);
            log.setNewBankBal(newBankBal);
            log.setOldEssentialsBal(oldEssentialsBal.intValue());
            log.setNewEssentialsBal(newEssentialsBal.intValue());
            log.print();
        }
    }
}
