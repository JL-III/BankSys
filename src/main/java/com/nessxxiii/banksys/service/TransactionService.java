package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionType;
import com.nessxxiii.banksys.exceptions.DatabaseOperationException;
import com.nessxxiii.banksys.logging.TransactionLogger;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static com.nessxxiii.banksys.utils.Formatter.formatBalance;

public class TransactionService {
    private final PlayerBalanceDAO playerBalanceDAO;
    private final Economy economy;
    private final TransactionProcessor transactionProcessor;

    public TransactionService(Economy economy, PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger) {
        this.economy = economy;
        this.playerBalanceDAO = playerBalanceDAO;
        this.transactionProcessor = new TransactionProcessor(economy, new TransactionLogger(), playerBalanceDAO, customLogger);
    }

    //Used for the balance command
    public String inquiry(UUID playerUUID) {
        try {
            Optional<Integer> optionalPlayerBalance = playerBalanceDAO.findPlayerBalance(playerUUID);
            if (optionalPlayerBalance.isPresent()) {
                return formatBalance(optionalPlayerBalance.get());
            } else {
                throw new DatabaseOperationException("No player found");
            }
        } catch (SQLException | DatabaseOperationException e) {
            Bukkit.getConsoleSender().sendMessage("BankSys: "+ e.getMessage());
            return "No bank balance exists.";
        }
    }

    public String deposit(OfflinePlayer player, int amount) {
       return transactionProcessor.processTransaction(player, amount, TransactionType.DEPOSIT, economy::withdrawPlayer);
    }

    public String withdraw(OfflinePlayer player, int amount) {
       return transactionProcessor.processTransaction(player, amount, TransactionType.WITHDRAWAL, economy::depositPlayer);
    }

}
