package com.tomkeuper.bedwars.proxy.utils;

import com.google.gson.JsonObject;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeedbackManager {

    private static FeedbackManager instance;
    private final Map<UUID, Long> feedbackCooldown = new HashMap<>();
    private static final long COOLDOWN_MS = 60000; // 1 minute cooldown

    private FeedbackManager() {}

    public static FeedbackManager getInstance() {
        if (instance == null) {
            instance = new FeedbackManager();
        }
        return instance;
    }

    /**
     * Prompts a player to enter feedback in chat
     */
    public void promptFeedback(Player player) {
        // Check cooldown
        if (feedbackCooldown.containsKey(player.getUniqueId())) {
            long timeSinceLastFeedback = System.currentTimeMillis() - feedbackCooldown.get(player.getUniqueId());
            if (timeSinceLastFeedback < COOLDOWN_MS) {
                long secondsRemaining = (COOLDOWN_MS - timeSinceLastFeedback) / 1000;
                player.sendMessage(IridiumColorAPI.process("&c&l» &cPlease wait " + secondsRemaining + " seconds before submitting feedback again."));
                return;
            }
        }

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(IridiumColorAPI.process("&b&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(IridiumColorAPI.process("&b&l          FEEDBACK SYSTEM"));
        player.sendMessage("");
        player.sendMessage(IridiumColorAPI.process("&7We'd love to hear your thoughts!"));
        player.sendMessage(IridiumColorAPI.process("&7Please type your feedback, bug report, or suggestion"));
        player.sendMessage(IridiumColorAPI.process("&7in the chat below. Your message will be sent"));
        player.sendMessage(IridiumColorAPI.process("&7directly to our development team."));
        player.sendMessage("");
        player.sendMessage(IridiumColorAPI.process("&e&l» &eType your feedback now:"));
        player.sendMessage(IridiumColorAPI.process("&b&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage("");

        // Register player as waiting for feedback
        FeedbackChatListener.registerPlayer(player);
    }

    /**
     * Submits feedback to Discord via webhook
     */
    public void submitFeedback(Player player, String feedback) {
        // Set cooldown
        feedbackCooldown.put(player.getUniqueId(), System.currentTimeMillis());

        // Send confirmation to player
        player.sendMessage("");
        player.sendMessage(IridiumColorAPI.process("&a&l✓ &aThank you for your feedback!"));
        player.sendMessage(IridiumColorAPI.process("&7Your message has been sent to our development team."));
        player.sendMessage("");

        // Get webhook URL from config
        String webhookUrl = BedWarsProxy.config.getYml().getString("discord-webhook-url", "");

        if (webhookUrl.isEmpty()) {
            BedWarsProxy.getPlugin().getLogger().warning("Discord webhook URL not configured! Feedback will not be sent.");
            BedWarsProxy.getPlugin().getLogger().info("Feedback from " + player.getName() + ": " + feedback);
            return;
        }

        // Send to Discord asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(BedWarsProxy.getPlugin(), () -> {
            try {
                sendToDiscord(webhookUrl, player, feedback);
            } catch (Exception e) {
                BedWarsProxy.getPlugin().getLogger().severe("Failed to send feedback to Discord: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void sendToDiscord(String webhookUrl, Player player, String feedback) throws Exception {
        URL url = new java.net.URI(webhookUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create Discord embed
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "📝 New Feedback Received");
        embed.addProperty("description", feedback);
        embed.addProperty("color", 3447003); // Blue color

        // Add fields
        JsonObject playerField = new JsonObject();
        playerField.addProperty("name", "Player");
        playerField.addProperty("value", player.getName());
        playerField.addProperty("inline", true);

        JsonObject uuidField = new JsonObject();
        uuidField.addProperty("name", "UUID");
        uuidField.addProperty("value", player.getUniqueId().toString());
        uuidField.addProperty("inline", true);

        JsonObject serverField = new JsonObject();
        serverField.addProperty("name", "Server");
        serverField.addProperty("value", org.bukkit.Bukkit.getServer().getName());
        serverField.addProperty("inline", true);

        com.google.gson.JsonArray fields = new com.google.gson.JsonArray();
        fields.add(playerField);
        fields.add(uuidField);
        fields.add(serverField);
        embed.add("fields", fields);

        // Add timestamp
        embed.addProperty("timestamp", java.time.Instant.now().toString());

        // Create embeds array
        com.google.gson.JsonArray embeds = new com.google.gson.JsonArray();
        embeds.add(embed);

        // Create final JSON
        JsonObject json = new JsonObject();
        json.addProperty("username", "BedWars Feedback");
        json.addProperty("avatar_url", "https://i.imgur.com/4M34hi2.png"); // Optional: Add your own icon URL
        json.add("embeds", embeds);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 204 || responseCode == 200) {
            BedWarsProxy.getPlugin().getLogger().info("Feedback from " + player.getName() + " sent to Discord successfully.");
        } else {
            BedWarsProxy.getPlugin().getLogger().warning("Failed to send feedback to Discord. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    /**
     * Clear cooldown for a player (for testing purposes)
     */
    public void clearCooldown(UUID uuid) {
        feedbackCooldown.remove(uuid);
    }
}

