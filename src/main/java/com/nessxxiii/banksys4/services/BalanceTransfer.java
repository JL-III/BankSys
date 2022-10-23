package com.nessxxiii.banksys4.services;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.PlayerBank;
import com.nessxxiii.banksys4.db.PlayersActiveToday;
import com.nessxxiii.banksys4.models.PlayerBalance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BalanceTransfer {
    private final Banksys4 plugin;
    private final ATM atm;
    private final PlayerBank playerBank;
    private final PlayersActiveToday playersActiveToday;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public BalanceTransfer(Banksys4 plugin) {
        this.plugin = plugin;
        this.atm = new ATM(plugin);
        this.playersActiveToday = new PlayersActiveToday(plugin);
        this.playerBank = new PlayerBank(plugin);
    }

    public void schedule() {
        if (scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        long midnight = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);

        if (plugin.getConfig().getBoolean("is-main-server")) {
            scheduler.scheduleAtFixedRate(this::withdrawBalances, midnight, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
        } else {
            long fiveMinutesBeforeMidnight = midnight - TimeUnit.MINUTES.toMillis(5);
            scheduler.scheduleAtFixedRate(this::depositBalances, fiveMinutesBeforeMidnight, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public void run() {
        if (plugin.getConfig().getBoolean("is-main-server")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::withdrawBalances);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::depositBalances);
        }
    }

    public void withdrawBalances() {
        List<PlayerBalance> balances;

        try {
            balances = playerBank.getBalances();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        for (PlayerBalance playerBalance : balances) {
            OfflinePlayer offlinePlayer;
            try {
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerBalance.getUuid()));
                atm.withdrawFromBank(offlinePlayer, playerBalance.getBalance());
            } catch (Exception ex){
                ex.printStackTrace();
            }

        }

    }

    public void depositBalances() {
        List<String> uuids;

        try {
            uuids = playersActiveToday.getActiveUUIDs();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        for (String uuid : uuids) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            double balance = plugin.getEconomy().getBalance(offlinePlayer);
            if (balance > 1) {
                atm.depositToBank(offlinePlayer, (int) balance);
            }
        }

        try {
            playersActiveToday.purge();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
