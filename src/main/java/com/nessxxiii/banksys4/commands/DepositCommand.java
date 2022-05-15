package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys2;
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

public class DepositCommand implements CommandExecutor {

    private static int amount;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player;
        if (sender instanceof Player){
            player = (Player) sender;
            UUID playerUUID = player.getUniqueId();
            Economy economy = Banksys2.getEconomy();




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

            if (Integer.parseInt(args[0]) > economy.getBalance(player)) {

                player.sendMessage(ChatColor.RED + "You do not have enough money for that transaction!");

                return false;
            }

                player.sendMessage(ChatColor.GREEN + "Deposit amount: " + ChatColor.YELLOW + args[0]);
                int amount = Integer.parseInt(args[0]);
                DataBase database = new DataBase();

                try {
                    if (database.findPlayerBalanceByUUID(playerUUID.toString()) != null) {
                        database.updatePlayerBalance(playerUUID.toString(),amount);
                        economy.withdrawPlayer(player,amount);
                        player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
                        player.sendMessage( ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + database.findPlayerBalanceByUUID(playerUUID.toString()).getBalance());
                    } else {
                        database.createPlayerBalance(playerUUID.toString());
                        database.updatePlayerBalance(playerUUID.toString(),amount);
                        economy.withdrawPlayer(player,amount);
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

    public static int getAmount(){
        return amount;
    }

}
