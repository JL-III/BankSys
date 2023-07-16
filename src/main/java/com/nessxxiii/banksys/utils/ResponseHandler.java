package com.nessxxiii.banksys.utils;

import org.bukkit.ChatColor;

import static com.nessxxiii.banksys.utils.Formatter.formatAmount;
import static com.nessxxiii.banksys.utils.Formatter.formatBalance;

public class ResponseHandler {

    private static final String INSUFFICIENT_FUNDS_MESSAGE = ChatColor.RED + "You do not have sufficient funds!\nDeposit Requested: ";
    public static final String WALLET_BALANCE_MESSAGE = ChatColor.RED + "Wallet: ";
    public static final String TRANSACTION_SUCCESS_MESSAGE = ChatColor.GREEN + "Successful ";
    public static final String WALLET_BALANCE = "Wallet: ";
    public static final String BANK_BALANCE_MESSAGE = "Bank Balance: ";
    public static final String ERROR_MESSAGE = ChatColor.RED + "There was an error processing this command - Contact an admin immediately for help!\n";

    // Generate a message for insufficient funds
    public static String buildInsufficientFundsMessage(int amount, double oldEssentialsBal) {
        return INSUFFICIENT_FUNDS_MESSAGE
                + formatAmount(amount) + "\n"
                + WALLET_BALANCE_MESSAGE
                + formatBalance(oldEssentialsBal);
    }

    // Generate an error message
    public static String buildErrorMessage(int amount, int oldBankBal) {
        return ChatColor.RED
                + "An error occurred, do you have sufficient funds?" + ChatColor.YELLOW + "\n"
                + "Requested Amount: " + formatAmount(amount) + ChatColor.LIGHT_PURPLE + "\n"
                + BANK_BALANCE_MESSAGE + formatBalance(oldBankBal);
    }

}
