package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.DataBase;
import com.nessxxiii.banksys4.models.ConsoleLogTransaction;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerDeposit {

    public void onCommand(Player player, Economy economy, int amount,Double oldEssentialsBal) {

        String name = player.getName();
        String transactionType = "Deposit";
        UUID playerUUID = player.getUniqueId();
        ConsoleLogTransaction currentTransaction = new ConsoleLogTransaction();


        if (amount > oldEssentialsBal) {
            player.sendMessage(ChatColor.RED + "Insufficient Funds.");
            player.sendMessage(ChatColor.GREEN + "Amount requested: " + ChatColor.YELLOW + amount);
            player.sendMessage(ChatColor.GREEN + "Pocket balance: " + ChatColor.RED + oldEssentialsBal.intValue());
            currentTransaction.PrintInfoToConsole(player.getName(),amount, transactionType, oldEssentialsBal, "Insufficient Funds");
            return;
        }

        DataBase database = new DataBase(Banksys4.getPlugin());
        String UUID = playerUUID.toString();
        int oldBal;
        int newBal;
        EconomyResponse response = economy.withdrawPlayer(player,amount);

        if (response.transactionSuccess()){
            try {
                database.createPlayerBalanceIfNotExists(UUID);
                oldBal = database.findPlayerBalance(UUID).getBalance();
                database.updatePlayerBalance(UUID,amount);
                newBal = database.findPlayerBalance(UUID).getBalance();

            }catch (Exception ex){
                player.sendMessage(ChatColor.RED + "There was an error depositing money, please let an administrator know. ErrorCodeD1");
                economy.depositPlayer(player,amount);
                ex.printStackTrace();
                return;
            }
            Double newEssentialsBal = economy.getBalance(player);
            if (oldBal + amount == newBal) {
                player.sendMessage(ChatColor.GREEN + "Deposit amount: " + ChatColor.YELLOW + amount);
                player.sendMessage(ChatColor.GREEN + "PocketBal: " + newEssentialsBal);
                player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + newBal);
                currentTransaction.PrintInfoToConsole(name,amount,transactionType,oldBal,newBal,oldEssentialsBal.intValue(),newEssentialsBal.intValue(),"Transaction Complete");
            } else {
                player.sendMessage(ChatColor.RED + "There was an error depositing money, please let an administrator know. ErrorCodeD2");
                currentTransaction.PrintInfoToConsole(name,amount, transactionType,oldBal,newBal,oldEssentialsBal.intValue(),newEssentialsBal.intValue(),"ErrorCodeD3");

            }
        } else {
            player.sendMessage(ChatColor.RED + "There was an error depositing money, please let an administrator know. ErrorCodeD4");

        }

    }
}



