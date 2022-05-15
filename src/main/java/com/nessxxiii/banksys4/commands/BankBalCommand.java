package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.models.PlayerBalance;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class BankBalCommand implements CommandExecutor {

    private final Banksys4 plugin;

    public BankBalCommand(Banksys4 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player player;
        if (sender instanceof Player){
            player = (Player) sender;
            UUID playerUUID = player.getUniqueId();
            if (!player.hasPermission("bank.balance")){
                player.sendMessage("You do not have permission for that command!");
                return false;
            }
            if (args.length > 0) {
                return false;
            }

/*            player.sendMessage("uuid: " + playerUUID);*/


            try {

                PlayerBalance playerBalance = this.plugin.getDatabase().findPlayerBalanceByUUID(player.getUniqueId().toString());

                if (playerBalance == null) {
/*                    playerBalance = new PlayerBalance(player.getUniqueId().toString(), 0);*/
                    this.plugin.getDatabase().createPlayerBalance(playerUUID.toString());
                    player.sendMessage(ChatColor.GREEN + "BankBalance: " + ChatColor.YELLOW + "0");
                    return true;
                } else {
                    player.sendMessage(ChatColor.GREEN + "BankBalance: " + ChatColor.YELLOW + playerBalance.getBalance());
                    return true;

                }
            }catch (SQLException ex){

                ex.printStackTrace();

            }



            return false;

        }
        return false;
    }

}
