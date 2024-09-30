package com.nessxxiii.banksys.commands;

import com.nessxxiii.banksys.managers.ConfigManager;
import com.nessxxiii.banksys.managers.CooldownManager;
import com.nessxxiii.banksys.service.TransactionService;
import com.nessxxiii.banksys.utils.Validation;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class PlayerCommands implements CommandExecutor, TabCompleter {

    private final TransactionService transactionService;

    private final ConfigManager configManager;
    private final CustomLogger customLogger;
    private CooldownManager cooldownManager;

    private final String bankBalOtherPermission = "theatria.bank.bal.other";

    public PlayerCommands(TransactionService transactionService, ConfigManager configManager, CooldownManager cooldownManager, CustomLogger customLogger) {
        this.transactionService = transactionService;
        this.configManager = configManager;
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
            player.sendMessage( ChatColor.RED + "You must wait " + ChatColor.YELLOW + cooldownManager.getNextUse(player.getUniqueId()) + ChatColor.RED + " seconds to use the bank again!");
            return true;
        } else {
            cooldownManager.updateCooldown(player.getUniqueId());
        }

        if (player.hasPermission(bankBalOtherPermission)) {
            if (("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) && args.length == 2) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                Response<String> stringResponse = transactionService.inquiry(offlinePlayer.getUniqueId());
                if (stringResponse.isSuccess()) {
                    player.sendMessage(stringResponse.value());
                    return true;
                } else {
                    player.sendMessage(stringResponse.error());
                    customLogger.sendLog(stringResponse.error());
                }
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.bal.self")) {
            if ("balance".equalsIgnoreCase(args[0]) || ("bal".equalsIgnoreCase(args[0]))) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Bank Balance: " + ChatColor.YELLOW + transactionService.inquiry(player.getUniqueId()));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.deposit") && !configManager.isMainServer()) {
            if (args[0].equalsIgnoreCase("deposit") && args.length == 2) {
                Optional<Integer> amount = Validation.processPlayerInputAmount(args[1]);
                if (amount.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "You must provide a whole number greater than 0.");
                    return true;
                }
                player.sendMessage(transactionService.deposit(Bukkit.getOfflinePlayer(player.getUniqueId()), amount.get()));
                return true;
            }
        }

        if (player.hasPermission("theatria.bank.withdraw") && configManager.isMainServer()) {
            if (args[0].equalsIgnoreCase("withdraw") && args.length == 2) {
                Optional<Integer> amount = Validation.processPlayerInputAmount(args[1]);
                if (amount.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "You must provide a whole number greater than 0.");
                    return true;
                }
                player.sendMessage(transactionService.withdraw(Bukkit.getOfflinePlayer(player.getUniqueId()), amount.get()));
                return true;
            }
        }
        return true;
    }

    public void reloadConfigManager(ConfigManager configManager) {
        this.cooldownManager = new CooldownManager(configManager);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (args[0].equalsIgnoreCase("bal") && args.length == 2 && player.hasPermission(bankBalOtherPermission)) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
            if (args.length == 1) {
                if (configManager.isMainServer()) {
                    return List.of("bal", "withdraw");
                } else {
                    return List.of("bal", "deposit");
                }
            }
        }
        return null;
    }
}
