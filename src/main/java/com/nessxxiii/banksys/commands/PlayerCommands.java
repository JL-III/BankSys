package com.nessxxiii.banksys.commands;

import com.nessxxiii.banksys.managers.ConfigManager;
import com.nessxxiii.banksys.managers.CooldownManager;
import com.nessxxiii.banksys.managers.TransactionManager;
import com.playtheatria.jliii.generalutils.utils.PlayerMessenger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerCommands implements CommandExecutor {
    private final TransactionManager transactionManager;

    private final CooldownManager cooldownManager;

    public PlayerCommands(TransactionManager transactionManager, ConfigManager configManager) {
        this.transactionManager = transactionManager;
        this.cooldownManager = new CooldownManager(configManager);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Player validation
        if (!(sender instanceof Player player)) return false;
        if (args.length < 1) return false;

        if (!cooldownManager.isCooldownOver(player.getUniqueId())) {
            player.sendMessage("You must wait " + cooldownManager.getNextUse(player.getUniqueId()) + "to use the bank again!");
            return true;
        }

        if (player.hasPermission("theatria.bank.bal.other")) {
            if (("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) && args.length == 2) {
                player.sendMessage(transactionManager.inquireBankBalance(Bukkit.getOfflinePlayer(args[1])));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.bal.self")) {
            if ("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) {
                player.sendMessage(transactionManager.inquireBankBalance(Bukkit.getOfflinePlayer(player.getUniqueId())));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.deposit")) {
            if (args[0].equalsIgnoreCase("deposit") && args.length == 2) {
                transactionManager.depositToBank(Bukkit.getOfflinePlayer(player.getUniqueId()), Integer.parseInt(args[1]));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.withdraw")) {
            if (args[0].equalsIgnoreCase("withdraw") && args.length == 2) {
                player.sendMessage("Sent request to withdraw --- dummy message need to implement.");
//                ATM atm = new ATM(plugin);
//                player.sendMessage(atm.inquireBankBalance(Bukkit.getOfflinePlayer(player.getUniqueId())));
                return true;
            }
        }

        return true;
    }
}
