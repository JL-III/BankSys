package com.nessxxiii.banksys4.listeners;

import com.nessxxiii.banksys4.Banksys4;
import com.nessxxiii.banksys4.db.PlayersActiveToday;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class ActivePlayersListener implements Listener {
    private final Banksys4 plugin;
    private final PlayersActiveToday playersActiveToday;

    public ActivePlayersListener(Banksys4 plugin) {
        this.plugin = plugin;
        playersActiveToday = new PlayersActiveToday(plugin);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        String name = event.getPlayer().getName();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                playersActiveToday.upsert(uuid, name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
