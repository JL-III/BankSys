package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.services.ATM;
//import com.nessxxiii.banksys4.services.BalanceTransfer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankCommands implements CommandExecutor {
//    private final BalanceTransfer balanceTransfer;
    private final Banksys4 plugin;

    public BankCommands(Banksys4 plugin) {
//        this.balanceTransfer = new BalanceTransfer(plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Player validation
        if (!(sender instanceof Player player)) return false;
        if (args.length < 1) return false;
        // Permission validation
//        if (player.hasPermission("theatria.bank.transfer")) {
//            if ("transfer".equalsIgnoreCase(args[0])) {
//                balanceTransfer.run();
//                player.sendMessage(ChatColor.GREEN + "Balance transfer is running.");
//                return true;
//            }
//        }
        if (player.hasPermission("theatria.bank.bal.other")) {
            if (("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) && args.length == 2) {
                ATM atm = new ATM(plugin);
                player.sendMessage(atm.inquireBankBalance(Bukkit.getOfflinePlayer(args[1])));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.bal.self")) {
            if ("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) {
                ATM atm = new ATM(plugin);
                player.sendMessage(atm.inquireBankBalance(Bukkit.getOfflinePlayer(player.getUniqueId())));
                return true;
            }
        }

        return true;
    }
}
