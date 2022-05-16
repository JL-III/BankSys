package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.DataBase;
import com.nessxxiii.banksys4.models.ConsoleLogTransaction;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class WithdrawCommand implements CommandExecutor {

    private HashMap<UUID,Long> coolDown = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            Bukkit.getServer().getConsoleSender().sendMessage("You must be a player to use this command");
            return false;
        }
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Economy economy = Banksys4.getEconomy();

        if (!player.hasPermission("bank.deposit")){
            player.sendMessage("You do not have permission for that command!");
            return false;
        }
        if (args.length == 0) {
            player.sendMessage("You need to provide an amount!");
            return false;
        }

        Integer amount = 0;
        try {
            Integer.parseInt(args[0]);
            amount = Integer.parseInt(args[0]);
        } catch(NumberFormatException e){
            player.sendMessage("You need to enter a whole number");
            return false;

        }

        if (amount > 1000000000) {
            player.sendMessage("Exceeds deposit amount, must be an integer below 1,000,000,000");
            return false;
        }

        if (args.length == 1){
            if (amount < 1) {
                player.sendMessage( ChatColor.RED + "Amount must be a positive integer!");
                return false;
            }

            ConsoleLogTransaction currentTransaction = new ConsoleLogTransaction();
            DataBase database = new DataBase();
            String name = player.getName();
            String UUID = playerUUID.toString();
            Long time = System.currentTimeMillis();
            int oldBankBal;
            int newBankBal;
            Double oldEssentialsBal;
            Double newEssentialsBal;

            try {
                database.createPlayerBalanceIfNotExists(UUID);
                oldBankBal = database.findPlayerBalance(UUID).getBalance();

                if ((oldBankBal - amount) < 0 ) {
                    player.sendMessage(ChatColor.RED + "You dont have enough money.");
                    currentTransaction.PrintInfoToConsoleNSFunds(name, amount, oldBankBal,"Insufficient Funds");
                    /*coolDown.putIfAbsent(playerUUID,time);*/
                    return false;
                }
                database.updatePlayerBalance(UUID,-amount);
                newBankBal = database.findPlayerBalance(UUID).getBalance();

            }catch (Exception ex){
                /*ex.printStackTrace();*/
                currentTransaction.PrintInfoToConsoleFailAtDataBase(name,amount,"Failed ErrorCode:W1" );
                player.sendMessage(ChatColor.RED + "There was an error withdrawing money, please let an administrator know. ErrorCode:W1");
                return false;
            }
            oldEssentialsBal = economy.getBalance(player);
            EconomyResponse response = economy.depositPlayer(player, amount.doubleValue());
            newEssentialsBal = economy.getBalance(player);
            if (response.transactionSuccess()){
                player.sendMessage(ChatColor.GREEN + "Withdraw amount: "+ ChatColor.RED + amount);
                player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
                player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + newBankBal);
                currentTransaction.PrintInfoToConsoleSuccess(name,amount,oldBankBal,newBankBal,oldEssentialsBal.intValue(),newEssentialsBal.intValue(),"Transaction Complete");

            } else {
                player.sendMessage(ChatColor.RED + "There was an error withdrawing money, please let an administrator know. ErrorCode:W2");
                currentTransaction.PrintInfoToConsoleFailAtEssentials(name,amount,oldBankBal,newBankBal,oldEssentialsBal.intValue(),newEssentialsBal.intValue(),"Failed at Essentials response");
                return false;
            }
            return true;
        }
        return true;

    }
}
