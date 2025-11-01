package com.tomkeuper.bedwars.proxy.utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener that handles join and quit events.
 * Disables the default join and quit messages.
 */
public class JoinQuitListener implements Listener {

    /**
     * Handles player join events.
     * Sets the join message to null to completely disable it.
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Completely disable the default join message
        event.setJoinMessage(null);
    }

    /**
     * Handles player quit events.
     * Sets the quit message to null to completely disable it.
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Completely disable the default quit message
        event.setQuitMessage(null);
    }
}

