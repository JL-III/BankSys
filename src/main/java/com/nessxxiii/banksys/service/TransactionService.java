package com.nessxxiii.banksys.service;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import com.nessxxiii.banksys.enums.TransactionType;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.Response;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

import static com.nessxxiii.banksys.utils.Formatter.formatBalance;

public class TransactionService {
    private final PlayerBalanceDAO playerBalanceDAO;
    private final Economy economy;
    private final TransactionProcessor transactionProcessor;

    public TransactionService(Economy economy, PlayerBalanceDAO playerBalanceDAO, CustomLogger customLogger) {
        this.economy = economy;
        this.playerBalanceDAO = playerBalanceDAO;
        this.transactionProcessor = new TransactionProcessor(economy, playerBalanceDAO, customLogger);
    }

    //Used for the balance command
    public Response<String> inquiry(UUID playerUUID) {
        Response<Integer> integerResponse = playerBalanceDAO.findPlayerBalance(playerUUID);
        if (integerResponse.isSuccess()) {
            return Response.success(formatBalance(integerResponse.value()));
        } else {
            return Response.failure(integerResponse.error());
        }
    }

    public String deposit(OfflinePlayer player, int amount) {
       return transactionProcessor.processTransaction(player, amount, TransactionType.DEPOSIT, economy::withdrawPlayer);
    }

    public String withdraw(OfflinePlayer player, int amount) {
       return transactionProcessor.processTransaction(player, amount, TransactionType.WITHDRAWAL, economy::depositPlayer);
    }

}
