package com.nessxxiii.banksys.listeners;

import com.nessxxiii.banksys.dao.PlayerBalanceDAO;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final PlayerBalanceDAO playerBalanceDAO;

    public PlayerJoin(PlayerBalanceDAO playerBalanceDAO) {
        this.playerBalanceDAO = playerBalanceDAO;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        playerBalanceDAO.createPlayerBalanceIfNotExists(event.getPlayer().getUniqueId());
    }

}
