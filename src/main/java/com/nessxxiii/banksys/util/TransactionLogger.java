package com.nessxxiii.banksys.util;

import com.nessxxiii.banksys.data.TransactionLog;
import com.nessxxiii.banksys.enums.TransactionStatus;
import com.nessxxiii.banksys.enums.TransactionType;

public class TransactionLogger {
    public void logTransaction(String name, Integer amount, Integer oldBankBal, Integer newBankBal, double oldEssentialsBal, double newEssentialsBal, TransactionType transactionType, TransactionStatus transactionStatus) {
        TransactionLog log = new TransactionLog(name, amount, transactionType, transactionStatus);
        log.setOldBankBal(oldBankBal);
        log.setNewBankBal(newBankBal);
        log.setOldEssentialsBal((int) oldEssentialsBal);
        log.setNewEssentialsBal((int) newEssentialsBal);
        log.print();
    }

    public void logTransaction(String name, Integer amount, TransactionType transactionType, TransactionStatus transactionStatus) {
        new TransactionLog(name, amount, transactionType, transactionStatus).print();
    }

}
