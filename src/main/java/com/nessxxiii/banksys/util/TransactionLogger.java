package com.nessxxiii.banksys.util;

import com.nessxxiii.banksys.data.TransactionLog;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;
import org.bukkit.Bukkit;

import java.util.UUID;

public class TransactionLogger {
    public void logTransaction(UUID playerUUID, Integer amount, Integer oldBankBal, Integer newBankBal, double oldEssentialsBal, double newEssentialsBal, TransactionType transactionType, TransactionStatus transactionStatus) {
        String name = Bukkit.getOfflinePlayer(playerUUID).getName();
        TransactionLog log = new TransactionLog(name, amount, transactionType, transactionStatus);
        log.setOldBankBal(oldBankBal);
        log.setNewBankBal(newBankBal);
        log.setOldEssentialsBal((int) oldEssentialsBal);
        log.setNewEssentialsBal((int) newEssentialsBal);
        log.print();
    }

    public void logTransaction(UUID playerUUID, Integer amount, TransactionType transactionType, TransactionStatus transactionStatus) {
        String name = Bukkit.getOfflinePlayer(playerUUID).getName();
        new TransactionLog(name, amount, transactionType, transactionStatus).print();
    }

}
