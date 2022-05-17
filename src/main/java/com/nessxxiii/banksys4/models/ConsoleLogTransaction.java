package com.nessxxiii.banksys4.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ConsoleLogTransaction {

    private String name;
    private int amount;
    private int oldBankBal;
    private int newBankBal;
    private int oldEssentialsBal;
    private int newEssentialsBal;
    private String transactionStatus;

    public void PlayerTransactionInformation(String name,int amount,int oldBankBal,String transactionStatus){
        this.name = name;
        this.amount = amount;
        this.oldBankBal = oldBankBal;
        this.transactionStatus = transactionStatus;
    }

    public void PlayerTransactionInformation(String name,int amount,int oldBankBal,int newBankBal,int oldEssentialsBal, int newEssentialsBal,String transactionStatus){
        this.name = name;
        this.amount = amount;
        this.oldBankBal = oldBankBal;
        this.newBankBal = newBankBal;
        this.oldEssentialsBal = oldEssentialsBal;
        this.newEssentialsBal = newEssentialsBal;
        this.transactionStatus = transactionStatus;
    }

    public void PrintInfoToConsole(String name, int amount, String transactionType, int oldBankBal, int newBankBal, int oldEssentialsBal, int newEssentialsBal, String transactionStatus){

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Player: " + name + ChatColor.YELLOW + " | " + transactionType + " Request: " + ChatColor.GREEN + amount);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Bank balance at time of request: " + ChatColor.GREEN + oldBankBal);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Bank balance new balance after withdraw: " + ChatColor.GREEN + newBankBal);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Essentials balance before withdrawal: " + ChatColor.GREEN + oldEssentialsBal);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Essentials balance after withdrawal: " + ChatColor.GREEN + newEssentialsBal);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Transaction Status: " + ChatColor.GREEN + transactionStatus);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");


    }

    public void PrintInfoToConsole(String name, int amount, String transactionType, String transactionStatus){

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Player: " + name + ChatColor.YELLOW + " | " + transactionType + " Request: " + ChatColor.GREEN + amount);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Transaction Status: " + ChatColor.RED + transactionStatus);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");

    }


    public void PrintInfoToConsole(String name, int amount, String transactionType, int oldBankBal, String transactionStatus){

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Player: " + name + ChatColor.YELLOW + " | " + transactionType + " Request: " + ChatColor.GREEN + amount);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Bank balance at time of request: " + ChatColor.GREEN + oldBankBal);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Transaction Status: " + ChatColor.RED + transactionStatus);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");

    }

    public void PrintInfoToConsole(String name, int amount, String transactionType, double oldEssentialsBal, String transactionStatus){

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Player: " + name + ChatColor.YELLOW + " | " + transactionType + " Request: " + ChatColor.GREEN + amount);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Essentials balance at time of request: " + ChatColor.GREEN + oldEssentialsBal);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Transaction Status: " + ChatColor.RED + transactionStatus);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");

    }

    public void PrintInfoToConsoleInquiry(String name, int oldBankBal, String transactionType, String transactionStatus){

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Player: " + name + ChatColor.YELLOW + " | " + transactionType + " Request: " + ChatColor.GREEN + oldBankBal);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Transaction Status: " + ChatColor.GREEN + transactionStatus);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "$--------Theatria-Bank-Slip--------$");

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getOldBankBal() {
        return oldBankBal;
    }

    public void setOldBankBal(int oldBankBal) {
        this.oldBankBal = oldBankBal;
    }

    public int getNewBankBal() {
        return newBankBal;
    }

    public void setNewBankBal(int newBankBal) {
        this.newBankBal = newBankBal;
    }

    public int getOldEssentialsBal() {
        return oldEssentialsBal;
    }

    public void setOldEssentialsBal(int oldEssentialsBal) {
        this.oldEssentialsBal = oldEssentialsBal;
    }

    public int getNewEssentialsBal() {
        return newEssentialsBal;
    }

    public void setNewEssentialsBal(int newEssentialsBal) {
        this.newEssentialsBal = newEssentialsBal;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
