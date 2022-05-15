package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.DataBase;
import net.milkbowl.vault.economy.Economy;
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
        Player player;
        if (sender instanceof Player){
            player = (Player) sender;
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
            if (args[0].length() > 10) {
                player.sendMessage("Exceeds deposit amount, must be an integer below 1,000,000,000");
                return false;
            }
            if (args.length == 1 && isInt(args[0])){

                if (Integer.parseInt(args[0]) < 1) {
                    player.sendMessage( ChatColor.RED + "Amount must be a positive integer!");
                    return false;
                }

                player.sendMessage(ChatColor.GREEN + "Withdraw amount: "+ ChatColor.RED + args[0]);
                Integer amount = Integer.parseInt(args[0]);
                DataBase database = new DataBase();

                try {
                    if (database.findPlayerBalanceByUUID(playerUUID.toString()) != null) {
                        if ((database.findPlayerBalanceByUUID(playerUUID.toString()).getBalance() - amount) < 0 ) {
                            player.sendMessage(ChatColor.RED + "Yoooo you dont have enough money man");
                            return false;
                        }
                        database.updatePlayerBalance(playerUUID.toString(),-amount);
                        economy.depositPlayer(player,amount.doubleValue());
                        player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
                        player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + database.findPlayerBalanceByUUID(playerUUID.toString()).getBalance());
                    } else {
                        database.createPlayerBalance(playerUUID.toString());
                        if ((database.findPlayerBalanceByUUID(playerUUID.toString()).getBalance() - amount) < 0 ) {
                            player.sendMessage(ChatColor.RED + "Yoooo you dont have enough money man");
                            return false;
                        }

                        database.updatePlayerBalance(playerUUID.toString(),-amount);
                        economy.depositPlayer(player,amount.doubleValue());
                        player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
                        player.sendMessage( ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + database.findPlayerBalanceByUUID(playerUUID.toString()).getBalance());
                    }
                }catch (SQLException ex){
                    ex.printStackTrace();
                }
                return true;
            }
            return false;

        }
        return true;
    }

    public static boolean isInt(String string){

        try {
            Integer.parseInt(string);
        } catch(NumberFormatException nfe) {
            Bukkit.getServer().getConsoleSender().sendMessage("Player deposit amount is too large!");
/*            nfe.printStackTrace();*/
            return false;
        }
        return true;
    }
}
