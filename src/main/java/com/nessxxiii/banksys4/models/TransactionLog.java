package com.nessxxiii.banksys4.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class TransactionLog {
    private final String playerName;
    private final Integer amount;
    private final TransactionStatus transactionStatus;
    private final TransactionType transactionType;
    private Integer oldBankBal;
    private Integer newBankBal;
    private Integer oldEssentialsBal;
    private Integer newEssentialsBal;

    public TransactionLog(String playerName, Integer amount, TransactionType transactionType, TransactionStatus transactionStatus) {
        this.playerName = playerName;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
    }

    private static final String DIVIDER = "§a$--------Theatria-Bank-Slip--------$";

    // 1: Player name, 2: Transaction Type, 3: Amount
    private static final String TRANSACTION_INFO = "§ePlayer: %s | Request: %s §a$%s";

    // 1: Bank balance at time of request
    private static final String BANK_BALANCE_BEFORE = "§ePrevious bank balance: §a$%s";

    // 1: Current bank balance
    private static final String BANK_BALANCE_AFTER = "§eNew bank balance: §a$%s";

    // 1: Essentials balance at time of request
    private static final String ESS_BALANCE_BEFORE = "§ePrevious essentials balance: §a$%s";

    // 1: Current essentials balance
    private static final String ESS_BALANCE_AFTER = "§eNew essentials balance: §a$%s";

    // 1: Color, 2: Transaction status
    private static final String TRANSACTION_STATUS = "%sTransaction Status: %s";

    private void printToConsole(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    private void printTransactionStatus(TransactionStatus transactionStatus) {
        ChatColor transactionStatusColor = switch (transactionStatus) {
            case SUCCESS -> ChatColor.GREEN;
            case INSUFFICIENT_FUNDS -> ChatColor.YELLOW;
            default -> ChatColor.RED;
        };

        printToConsole(String.format(TRANSACTION_STATUS, transactionStatusColor, transactionStatus));
    }

    public void print() {
        printToConsole(DIVIDER);
        printToConsole(String.format(TRANSACTION_INFO, this.playerName, this.transactionType, this.amount));

        if (oldBankBal != null) {
            printToConsole(String.format(BANK_BALANCE_BEFORE, oldBankBal));
        }

        if (newBankBal != null) {
            printToConsole(String.format(BANK_BALANCE_AFTER, newBankBal));
        }

        if (oldEssentialsBal != null) {
            printToConsole(String.format(ESS_BALANCE_BEFORE, oldEssentialsBal));
        }

        if (newEssentialsBal != null) {
            printToConsole(String.format(ESS_BALANCE_AFTER, newEssentialsBal));
        }

        printTransactionStatus(this.transactionStatus);
        printToConsole(DIVIDER);
    }

    public String getPlayerName() {
        return playerName;
    }

    public Integer getAmount() {
        return amount;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Integer getOldBankBal() {
        return oldBankBal;
    }

    public Integer getNewBankBal() {
        return newBankBal;
    }

    public Integer getOldEssentialsBal() {
        return oldEssentialsBal;
    }

    public Integer getNewEssentialsBal() {
        return newEssentialsBal;
    }

    public void setOldBankBal(Integer oldBankBal) {
        this.oldBankBal = oldBankBal;
    }

    public void setNewBankBal(Integer newBankBal) {
        this.newBankBal = newBankBal;
    }

    public void setOldEssentialsBal(Integer oldEssentialsBal) {
        this.oldEssentialsBal = oldEssentialsBal;
    }

    public void setNewEssentialsBal(Integer newEssentialsBal) {
        this.newEssentialsBal = newEssentialsBal;
    }
}
