package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankCommands implements CommandExecutor {
    private final Banksys4 plugin;
    public BankCommands(Banksys4 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    //validate player and permissions
        if (!(sender instanceof Player player)) return false;
        if (!player.hasPermission("Theatria.bank.command")){
            player.sendMessage("You do not have permission for that command!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("balance")){
            if (player.hasPermission("TheatriaBank.command.balance")){
                new PlayerBalance(this.plugin).onCommand(player);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
        }

        if (args.length != 2) return false;
        Economy economy = Banksys4.getEconomy();
        Double oldEssentialsBal = economy.getBalance(player);
    //A dummy transaction to mark bank transactions and to check if economy is responding.
        EconomyResponse response = economy.withdrawPlayer(player,0);
        if (!response.transactionSuccess()) return false;


    //validate number
        int amount;
        try {
            Integer.parseInt(args[1]);
            amount = Integer.parseInt(args[1]);
            if (amount < 1000 || amount > 1000000) {
                player.sendMessage(ChatColor.RED + "Amount must be between 1,000 and 1,000,000");
                return true;
            }
        } catch (Exception ex){
            player.sendMessage(ChatColor.RED + "You must enter a valid whole number.");
            player.sendMessage(ChatColor.YELLOW + "Example: /bank deposit 1000");
            return true;
        }
    //Determine subcommand and send to method
        String argString = args[0];
        switch (argString.toLowerCase()){
            case "deposit":
                if (player.hasPermission("TheatriaBank.command.deposit")){
                    new PlayerDeposit().onCommand(player,economy,amount,oldEssentialsBal);
                    break;
                }
                player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                break;
                //
            case ("withdraw"):
                if (player.hasPermission("TheatriaBank.command.withdraw")){
                    new PlayerWithdraw().onCommand(player,economy,amount,oldEssentialsBal);
                    break;
                }
                player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                break;
            default: return false;

        }
        return true;

    }
}
