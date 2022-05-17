package com.nessxxiii.banksys4.commands;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.models.ConsoleLogTransaction;
import com.nessxxiii.banksys4.models.PlayerTransactionInfo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerBalance {

    private final Banksys4 plugin;
    public PlayerBalance(Banksys4 plugin) {
        this.plugin = plugin;
    }

    public void onCommand(Player player) {

        UUID playerUUID = player.getUniqueId();
        String UUID = playerUUID.toString();
        ConsoleLogTransaction currentTransaction = new ConsoleLogTransaction();

        try {
            PlayerTransactionInfo playerTransactionInfo = this.plugin.getDatabase().findPlayerBalance(UUID);
            if (playerTransactionInfo == null) {
                this.plugin.getDatabase().createPlayerBalanceIfNotExists(UUID);
                player.sendMessage(ChatColor.GREEN + "BankBalance: " + ChatColor.YELLOW + "0");
                currentTransaction.PrintInfoToConsoleInquiry(player.getName(), 0, "Inquiry","Success");
            } else {
                player.sendMessage(ChatColor.GREEN + "BankBalance: " + ChatColor.YELLOW + playerTransactionInfo.getBalance());
                currentTransaction.PrintInfoToConsoleInquiry(player.getName(), playerTransactionInfo.getBalance(), "Inquiry","Success");

            }
        }catch (SQLException ex){
            ex.printStackTrace();
            player.sendMessage(ChatColor.RED + "Unable to retrieve balance, please let an administrator know. ErrorCode:B1");
            currentTransaction.PrintInfoToConsole(player.getName(),-1,"Inquiry","Failure");

        }
    }
}
