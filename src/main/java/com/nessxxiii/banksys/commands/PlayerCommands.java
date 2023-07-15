package com.nessxxiii.banksys.commands;

import com.nessxxiii.banksys.managers.ConfigManager;
import com.nessxxiii.banksys.managers.CooldownManager;
import com.nessxxiii.banksys.service.TransactionService;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerCommands implements CommandExecutor {
    private final TransactionService transactionService;
    private final CustomLogger customLogger;
    private CooldownManager cooldownManager;

    public PlayerCommands(TransactionService transactionService, CooldownManager cooldownManager, CustomLogger customLogger) {
        this.transactionService = transactionService;
        this.customLogger = customLogger;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Player validation
        if (sender instanceof ConsoleCommandSender console) {
            if (args.length == 2 && args[0].equalsIgnoreCase("bal")) {
                customLogger.sendLog("This has not been created yet");
            } else {
                customLogger.sendLog("You must use /bank bal <name> or /bank reload");
            }
        }
        if (!(sender instanceof Player player)) return false;
        if (args.length < 1) return false;

        if (!cooldownManager.isCooldownOver(player.getUniqueId())) {
            player.sendMessage("You must wait " + cooldownManager.getNextUse(player.getUniqueId()) + " seconds to use the bank again!");
            return true;
        } else {
            cooldownManager.updateCooldown(player.getUniqueId());
        }

        if (player.hasPermission("theatria.bank.bal.other")) {
            if (("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) && args.length == 2) {
                //TODO see what happens when you try to find a player that does not exist
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                player.sendMessage(transactionService.inquiry(offlinePlayer.getUniqueId()));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.bal.self")) {
            if ("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Bank Balance: " + ChatColor.YELLOW + transactionService.inquiry(player.getUniqueId()));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.deposit")) {
            if (args[0].equalsIgnoreCase("deposit") && args.length == 2) {
                player.sendMessage(transactionService.deposit(Bukkit.getOfflinePlayer(player.getUniqueId()), args[1]));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.withdraw")) {
            if (args[0].equalsIgnoreCase("withdraw") && args.length == 2) {
                player.sendMessage(transactionService.withdraw(Bukkit.getOfflinePlayer(player.getUniqueId()), args[1]));
                return true;
            }
        }

        return true;
    }

    public void reloadConfigManager(ConfigManager configManager) {
        this.cooldownManager = new CooldownManager(configManager);
    }
}
