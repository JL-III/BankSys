package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.DataBase;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class DepositCommand implements CommandExecutor {

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

            int amount = 0;
            if (args.length == 1) {
                try {
                    Integer.parseInt(args[0]);
                    amount = Integer.parseInt(args[0]);
                } catch (NumberFormatException ex){
                    player.sendMessage("You must enter a number");
                    return false;
                }
                if (amount < 1) {
                    player.sendMessage(ChatColor.RED + "Amount must be a positive integer!");
                    return false;
                }
            }

            if (amount > 1000000000) {
                player.sendMessage("Exceeds deposit amount, must be an integer below 1,000,000,000");
                return false;
            }

            if (amount > economy.getBalance(player)) {
                player.sendMessage(ChatColor.RED + "You do not have enough money for that transaction!");
                return false;
            }

            DataBase database = new DataBase();
            String UUID = playerUUID.toString();
            int oldBal;
            int newBal;
            EconomyResponse response = economy.withdrawPlayer(player,amount);

            if (response.transactionSuccess()){
                try {
                    database.createPlayerBalanceIfNotExists(UUID);
                    oldBal = database.findPlayerBalanceByUUID(UUID).getBalance();
                    database.updatePlayerBalance(UUID,amount);
                    newBal = database.findPlayerBalanceByUUID(UUID).getBalance();

                }catch (SQLException ex){
                    player.sendMessage("There was an error depositing money, please let an administrator know. ErrorCodeD1");
                    economy.depositPlayer(player,amount);
                    ex.printStackTrace();
                    return false;
                }

                if (oldBal + amount == newBal) {


                    player.sendMessage(ChatColor.GREEN + "Deposit amount: " + ChatColor.YELLOW + amount);
                    player.sendMessage(ChatColor.GREEN + "PocketBal: " + economy.getBalance(player));
                    player.sendMessage(ChatColor.GREEN + "BankBal: " + ChatColor.YELLOW + newBal);
                } else {
                    player.sendMessage("There was an error depositing money, please let an administrator know. ErrorCodeD2");
                    return false;
                }
            } else {
                player.sendMessage("There was an error depositing money, please let an administrator know. ErrorCodeD3");
                return true;
            }
            return true;
        }
        return false;

    }

}
