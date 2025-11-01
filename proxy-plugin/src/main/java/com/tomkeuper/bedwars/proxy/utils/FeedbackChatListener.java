package com.tomkeuper.bedwars.proxy.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FeedbackChatListener implements Listener {

    private static final Set<UUID> waitingForFeedback = new HashSet<>();

    /**
     * Register a player as waiting for feedback input
     */
    public static void registerPlayer(Player player) {
        waitingForFeedback.add(player.getUniqueId());
    }

    /**
     * Unregister a player from waiting for feedback
     */
    public static void unregisterPlayer(UUID uuid) {
        waitingForFeedback.remove(uuid);
    }

    /**
     * Check if a player is waiting to give feedback
     */
    public static boolean isWaitingForFeedback(UUID uuid) {
        return waitingForFeedback.contains(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (waitingForFeedback.contains(player.getUniqueId())) {
            e.setCancelled(true);

            String feedback = e.getMessage();

            // Remove player from waiting list
            waitingForFeedback.remove(player.getUniqueId());

            // Submit the feedback
            FeedbackManager.getInstance().submitFeedback(player, feedback);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Clean up if player quits while waiting for feedback
        waitingForFeedback.remove(e.getPlayer().getUniqueId());
    }
}

