package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.services.BalanceTransfer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankCommands implements CommandExecutor {
    private final BalanceTransfer balanceTransfer;

    public BankCommands(Banksys4 plugin) {
        this.balanceTransfer = new BalanceTransfer(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Player validation
        if (!(sender instanceof Player player)) return false;

        // Permission validation
        if (!player.hasPermission("theatria.bank.transfer")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return false;
        }

        if (args.length != 1 || !"transfer".equalsIgnoreCase(args[0])) {
            return false;
        }

        balanceTransfer.run();
        player.sendMessage(ChatColor.GREEN + "Balance transfer is running.");
        return true;
    }
}
