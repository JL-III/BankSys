package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankCommands implements CommandExecutor {
    private final Banksys4 plugin;
    private final ATM atm;

    public BankCommands(Banksys4 plugin) {
        this.plugin = plugin;
        this.atm = new ATM(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Player validation
        if (!(sender instanceof Player player)) return false;

        // Permission validation
        if (!player.hasPermission("Theatria.bank.command")) {
            player.sendMessage("You do not have permission for that command!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("balance")) {
            if (player.hasPermission("TheatriaBank.command.balance")) {
                atm.getBalance(player);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            }

            return true;
        }

        if (args.length != 2) return false;
        Economy economy = plugin.getEconomy();

        // Amount validation
        int amount;
        try {
            Integer.parseInt(args[1]);
            amount = Integer.parseInt(args[1]);
            if (amount < 1000 || amount > 1000000) {
                player.sendMessage(ChatColor.RED + "Amount must be between 1,000 and 1,000,000");
                return true;
            }
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "You must enter a valid whole number.");
            player.sendMessage(ChatColor.YELLOW + "Example: /bank deposit 1000");
            return true;
        }

        if ("deposit".equalsIgnoreCase(args[0])) {
            if (player.hasPermission("TheatriaBank.command.deposit")) {
                atm.deposit(player, amount);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            }

            return false;
        }

        if ("withdraw".equalsIgnoreCase(args[0])) {
            if (player.hasPermission("TheatriaBank.command.withdraw")) {
                atm.withdraw(player, amount);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            }

            return false;
        }

        return true;
    }
}
