package com.tomkeuper.bedwars.proxy.arenamanager;

import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

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
        String storedDate = BedWarsProxy.getRedisConnection().retrieveSetting("daily_event_date");
        String storedDayIndexStr = BedWarsProxy.getRedisConnection().retrieveSetting("daily_event_day_index");

        LocalDate today = LocalDate.now();
        String todayString = today.toString();

        // Check if we need to rotate to a new event
        if (storedDate == null || !storedDate.equals(todayString) || storedDayIndexStr == null) {
            // Need to select a new event
            rotateToNewEvent();
        } else {
            // Load the existing event for today using day index
            try {
                int dayIndex = Integer.parseInt(storedDayIndexStr);
                if (dayIndex >= 0 && dayIndex < allEvents.size()) {
                    currentEvent = allEvents.get(dayIndex);
                    BedWarsProxy.getPlugin().getLogger().info("Loaded daily event: " + currentEvent.getName() + " for " + todayString + " (Day " + (dayIndex + 1) + "/" + allEvents.size() + ")");
                } else {
                    // Invalid index, select a new one
                    rotateToNewEvent();
                }
            } catch (NumberFormatException e) {
                // Invalid stored index, select a new one
                rotateToNewEvent();
            }
        }
    }

    /**
     * Rotate to a new daily event (sequential rotation).
     */
    private void rotateToNewEvent() {
        String storedDayIndexStr = BedWarsProxy.getRedisConnection().retrieveSetting("daily_event_day_index");
        int currentDayIndex = 0;

        // Get the previous day index and increment it
        if (storedDayIndexStr != null) {
            try {
                currentDayIndex = Integer.parseInt(storedDayIndexStr);
                currentDayIndex = (currentDayIndex + 1) % allEvents.size(); // Move to next event, wrap around
            } catch (NumberFormatException e) {
                currentDayIndex = 0; // Start from beginning if invalid
            }
        }

        // Select the event based on sequential index
        currentEvent = allEvents.get(currentDayIndex);

        // Store in Redis
        LocalDate today = LocalDate.now();
        BedWarsProxy.getRedisConnection().storeSetting("daily_event_day_index", String.valueOf(currentDayIndex));
        BedWarsProxy.getRedisConnection().storeSetting("daily_event_date", today.toString());

        BedWarsProxy.getPlugin().getLogger().info("Rotated to new daily event: " + currentEvent.getName() + " for " + today + " (Day " + (currentDayIndex + 1) + "/" + allEvents.size() + ")");

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
     * Get the number of days until a specific event becomes active.
     * Returns 0 if the event is currently active.
     *
     * @param event The event to check
     * @return Number of days until the event is active
     */
    public int getDaysUntilEvent(DailyEvent event) {
        int currentDayIndex = getCurrentDayIndex();
        int eventIndex = allEvents.indexOf(event);

        if (eventIndex == -1) {
            return -1; // Event not found
        }

        if (eventIndex == currentDayIndex) {
            return 0; // Currently active
        }

        // Calculate days until this event
        int daysUntil;
        if (eventIndex > currentDayIndex) {
            daysUntil = eventIndex - currentDayIndex;
        } else {
            // Event is earlier in the list, so it wraps around
            daysUntil = (allEvents.size() - currentDayIndex) + eventIndex;
        }

        return daysUntil;
    }

    /**
     * Get the current day index in the rotation cycle.
     *
     * @return The current day index (0-based)
     */
    public int getCurrentDayIndex() {
        String storedDayIndexStr = BedWarsProxy.getRedisConnection().retrieveSetting("daily_event_day_index");
        if (storedDayIndexStr != null) {
            try {
                return Integer.parseInt(storedDayIndexStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
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

