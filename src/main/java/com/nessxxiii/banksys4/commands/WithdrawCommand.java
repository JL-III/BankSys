package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.DataBase;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class WithdrawCommand implements CommandExecutor {


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
                player.sendMessage("You need to enter a number");
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


                DataBase database = new DataBase();
                String UUID = playerUUID.toString();
                int oldBal;
                int newBal;

                try {
                    database.createPlayerBalanceIfNotExists(UUID);
                    oldBal = database.findPlayerBalanceByUUID(UUID).getBalance();
                    if ((oldBal - amount) < 0 ) {
                        player.sendMessage(ChatColor.RED + "Yoooo you dont have enough money man");
                        return false;
                    }
                    database.updatePlayerBalance(UUID,-amount);
                    newBal = database.findPlayerBalanceByUUID(UUID).getBalance();

                }catch (SQLException ex){
                    ex.printStackTrace();
                    player.sendMessage("There was an error withdrawing money, please let an administrator know. ErrorCode:W1");
                    return false;
                }
                if (oldBal - amount == newBal){
                    EconomyResponse response = economy.depositPlayer(player, amount.doubleValue());
                    if (response.transactionSuccess()){
                        player.sendMessage(ChatColor.GREEN + "Withdraw amount: "+ ChatColor.RED + amount);
                        player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
                        player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + newBal);
                    } else {
                        player.sendMessage("There was an error withdrawing money, please let an administrator know. ErrorCode:W2");
                        return false;
                    }

                } else {
                    player.sendMessage("There was an error withdrawing money, please let an administrator know. ErrorCode:W2");
                    return false;
                }
                return true;


        }
        return true;
    }
}
