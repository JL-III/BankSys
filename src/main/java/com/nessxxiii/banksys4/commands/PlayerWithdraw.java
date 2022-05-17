package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.db.DataBase;
import com.nessxxiii.banksys4.models.ConsoleLogTransaction;
import com.nessxxiii.banksys4.models.PlayerTransactionInfo;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.UUID;

public class PlayerWithdraw  {

    public void onCommand(Player player, Economy economy, int amount,Double oldEssentialsBal) {

        UUID playerUUID = player.getUniqueId();
        String transactionType = "Withdraw";
        ConsoleLogTransaction currentTransaction = new ConsoleLogTransaction();
        PlayerTransactionInfo thisTransaction;
        DataBase database = new DataBase();
        String name = player.getName();
        String UUID = playerUUID.toString();
        int oldBankBal;
        int newBankBal;
        Double newEssentialsBal;

        try {
            database.createPlayerBalanceIfNotExists(UUID);
            oldBankBal = database.findPlayerBalance(UUID).getBalance();

            if ((oldBankBal - amount) < 0 ) {
                player.sendMessage(ChatColor.RED + "Insufficient Funds.");
                player.sendMessage(ChatColor.GREEN + "Amount requested: " + ChatColor.YELLOW + amount);
                player.sendMessage(ChatColor.GREEN + "Bank balance: " + ChatColor.RED + oldBankBal);
                currentTransaction.PrintInfoToConsole(name, amount,transactionType ,oldBankBal,"Insufficient Funds");
                /*coolDown.putIfAbsent(playerUUID,time);*/
                return;
            }
            database.updatePlayerBalance(UUID,-amount);
            newBankBal = database.findPlayerBalance(UUID).getBalance();

        }catch (Exception ex){
            /*ex.printStackTrace();*/
            currentTransaction.PrintInfoToConsole(name,amount,transactionType,"Failed ErrorCode:W1" );
            player.sendMessage(ChatColor.RED + "There was an error withdrawing money, please let an administrator know. ErrorCode:W1");
            return;
        }
        oldEssentialsBal = economy.getBalance(player);
        EconomyResponse response = economy.depositPlayer(player, amount);
        newEssentialsBal = economy.getBalance(player);
        if (response.transactionSuccess()){
            player.sendMessage(ChatColor.GREEN + "Withdraw amount: "+ ChatColor.RED + amount);
            player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
            player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + newBankBal);
            currentTransaction.PrintInfoToConsole(name,amount,transactionType,oldBankBal,newBankBal,oldEssentialsBal.intValue(),newEssentialsBal.intValue(),"Transaction Complete");

        } else {
            player.sendMessage(ChatColor.RED + "There was an error withdrawing money, please let an administrator know. ErrorCode:W2");
            currentTransaction.PrintInfoToConsole(name,amount,transactionType,oldBankBal,newBankBal,oldEssentialsBal.intValue(),newEssentialsBal.intValue(),"Failed at Essentials response");
        }

    }

}
