package com.tomkeuper.bedwars.proxy.arenamanager;

import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the daily rotating event system for The Rift.
 * Events rotate once per day at midnight.
 */
public class DailyEventManager {

    private static DailyEventManager instance;
    private DailyEvent currentEvent;
    private BukkitTask rotationTask;
    private final List<DailyEvent> allEvents;

    private DailyEventManager() {
        this.allEvents = initializeEvents();
        loadCurrentEvent();
        scheduleRotation();
    }

    /**
     * Initialize the singleton instance.
     */
    public static void init() {
        if (instance == null) {
            instance = new DailyEventManager();
        }
    }

    /**
     * Get the singleton instance.
     */
    public static DailyEventManager getInstance() {
        return instance;
    }

    /**
     * Initialize all available daily events.
     */
    private List<DailyEvent> initializeEvents() {
        List<DailyEvent> events = new ArrayList<>();

        events.add(new DailyEvent(
            "Meteorite Storm",
            "METEOR_STORM",
            new String[]{
                "Meteorites rain from the sky!",
                "Collect rare &6Meteorite Fragments",
                "Random explosions around the map"
            },
            new String[]{
                "2x Meteorite Shard drops",
                "Random meteorite impacts",
                "Bonus resources from space rocks",
                "Fire resistance recommended"
            }
        ));

        events.add(new DailyEvent(
            "Gravity Shift",
            "GRAVITY_SHIFT",
            new String[]{
                "Experience altered gravity!",
                "Jump higher, fall slower",
                "New movement strategies"
            },
            new String[]{
                "Low gravity zones",
                "Enhanced jump boost III",
                "Slow falling effect",
                "Easier bridging"
            }
        ));

        events.add(new DailyEvent(
            "Cosmic Chaos",
            "COSMIC_CHAOS",
            new String[]{
                "Random events every 5 minutes!",
                "Expect the unexpected",
                "Double the rewards"
            },
            new String[]{
                "Random buffs and debuffs",
                "Mystery supply drops",
                "2x Crystal rewards",
                "Chaos orbs spawn randomly"
            }
        ));

        events.add(new DailyEvent(
            "Solar Flare",
            "SOLAR_FLARE",
            new String[]{
                "The sun unleashes its fury!",
                "Fire damage in open areas",
                "Seek shelter or burn"
            },
            new String[]{
                "Periodic fire damage outdoors",
                "Safe zones near Rift Cores",
                "Fire resistance upgrades available",
                "Bonus for staying in shade"
            }
        ));

        events.add(new DailyEvent(
            "Nebula Night",
            "NEBULA_NIGHT",
            new String[]{
                "Darkness falls across The Rift",
                "Limited visibility",
                "Night vision becomes essential"
            },
            new String[]{
                "Permanent night time",
                "Glowing ore deposits",
                "Stealth gameplay bonus",
                "Night vision potions available"
            }
        ));

        events.add(new DailyEvent(
            "Asteroid Belt",
            "ASTEROID_BELT",
            new String[]{
                "Navigate through asteroids!",
                "Floating islands appear",
                "New pathways to enemy bases"
            },
            new String[]{
                "Floating asteroid platforms",
                "Parkour challenges",
                "Bonus loot on asteroids",
                "New strategic routes"
            }
        ));

        events.add(new DailyEvent(
            "Warp Speed",
            "WARP_SPEED",
            new String[]{
                "Everything moves faster!",
                "Speed boost for all players",
                "Fast-paced action"
            },
            new String[]{
                "Permanent Speed III",
                "Faster mining & crafting",
                "Quick match mode",
                "Haste II effect"
            }
        ));

        return events;
    }

    /**
     * Load the current event from Redis or select a new one.
     */
    private void loadCurrentEvent() {
        String storedEventId = BedWarsProxy.getRedisConnection().retrieveSetting("daily_event_id");
        String storedDate = BedWarsProxy.getRedisConnection().retrieveSetting("daily_event_date");

        LocalDate today = LocalDate.now();
        String todayString = today.toString();

        // Check if we need to rotate to a new event
        if (storedDate == null || !storedDate.equals(todayString) || storedEventId == null) {
            // Need to select a new event
            rotateToNewEvent();
        } else {
            // Load the existing event for today
            currentEvent = findEventById(storedEventId);
            if (currentEvent == null) {
                // Event not found, select a new one
                rotateToNewEvent();
            } else {
                BedWarsProxy.getPlugin().getLogger().info("Loaded daily event: " + currentEvent.getName() + " for " + todayString);
            }
        }
    }

    /**
     * Rotate to a new daily event.
     */
    private void rotateToNewEvent() {
        // Select a random event (could be improved to ensure variety)
        int randomIndex = ThreadLocalRandom.current().nextInt(allEvents.size());
        currentEvent = allEvents.get(randomIndex);

        // Store in Redis
        LocalDate today = LocalDate.now();
        BedWarsProxy.getRedisConnection().storeSetting("daily_event_id", currentEvent.getId());
        BedWarsProxy.getRedisConnection().storeSetting("daily_event_date", today.toString());

        BedWarsProxy.getPlugin().getLogger().info("Rotated to new daily event: " + currentEvent.getName() + " for " + today);

        // Broadcast to all online players
        broadcastEventChange();

        // Broadcast to all game servers via Redis
        broadcastEventToGameServers();
    }

    /**
     * Schedule the automatic rotation at midnight.
     */
    private void scheduleRotation() {
        // Calculate ticks until next midnight
        long ticksUntilMidnight = getTicksUntilMidnight();

        // Schedule the rotation task
        rotationTask = Bukkit.getScheduler().runTaskTimer(BedWarsProxy.getPlugin(), () -> {
            rotateToNewEvent();
        }, ticksUntilMidnight, 24 * 60 * 60 * 20L); // 24 hours in ticks

        BedWarsProxy.getPlugin().getLogger().info("Scheduled daily event rotation. Next rotation in " + (ticksUntilMidnight / 20 / 60) + " minutes.");
    }

    /**
     * Calculate the number of ticks until midnight.
     */
    private long getTicksUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        Duration duration = Duration.between(now, nextMidnight);
        return duration.getSeconds() * 20; // Convert seconds to ticks (20 ticks per second)
    }

    /**
     * Find an event by its ID.
     */
    private DailyEvent findEventById(String id) {
        for (DailyEvent event : allEvents) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Broadcast the event change to all online players.
     */
    private void broadcastEventChange() {
        Bukkit.getScheduler().runTask(BedWarsProxy.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage("§8§m                                                    ");
                player.sendMessage("§b§lThe Rift §8» §6§lDaily Event Rotation");
                player.sendMessage("");
                player.sendMessage("§7Today's event: §e§l" + currentEvent.getName());
                player.sendMessage("");
                for (String line : currentEvent.getDescription()) {
                    player.sendMessage("§7" + line);
                }
                player.sendMessage("");
                player.sendMessage("§7Use §e/bw rotating §7to join!");
                player.sendMessage("§8§m                                                    ");
            });
        });
    }

    /**
     * Broadcast event change to all game servers via Redis.
     * This allows the arena plugin to update its active event in real-time.
     */
    private void broadcastEventToGameServers() {
        com.google.gson.JsonObject message = new com.google.gson.JsonObject();
        message.addProperty("type", "EVENT_SYNC");
        message.addProperty("event_id", currentEvent.getId());
        message.addProperty("event_name", currentEvent.getName());
        message.addProperty("timestamp", System.currentTimeMillis());

        // Send via Redis to all game servers
        BedWarsProxy.getRedisConnection().sendMessage(message, "bedwars-events");

        BedWarsProxy.getPlugin().getLogger().info(
            "Broadcasted event change to game servers: " + currentEvent.getName() + " (" + currentEvent.getId() + ")"
        );
    }

    /**
     * Get the current daily event.
     */
    public DailyEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Get all available events.
     */
    public List<DailyEvent> getAllEvents() {
        return new ArrayList<>(allEvents);
    }

    /**
     * Cancel the rotation task (for plugin disable).
     */
    public void shutdown() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
    }

    /**
     * Represents a daily rotating event.
     */
    public static class DailyEvent {
        private final String name;
        private final String id;
        private final String[] description;
        private final String[] features;

        public DailyEvent(String name, String id, String[] description, String[] features) {
            this.name = name;
            this.id = id;
            this.description = description;
            this.features = features;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String[] getDescription() {
            return description;
        }

        public String[] getFeatures() {
            return features;
        }
    }
}

