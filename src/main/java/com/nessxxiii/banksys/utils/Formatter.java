package com.nessxxiii.banksys.utils;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class Formatter {

    public static final NumberFormat formatter;

    static {
        formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
    }
    public static String formatAmount(int amount) {
        return ChatColor.YELLOW + formatter.format(amount);
    }

    public static String formatBalance(double balance) {
        return ChatColor.YELLOW + formatter.format(balance);
    }

    public static String getPlayerName(UUID uuid) {
        if (Bukkit.getOfflinePlayer(uuid).getName() != null) {
            return Bukkit.getOfflinePlayer(uuid).getName();
        } else {
            return "Unknown";
        }
    }
}
