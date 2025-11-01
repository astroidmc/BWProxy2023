package com.tomkeuper.bedwars.proxy.arenamanager;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import com.tomkeuper.bedwars.proxy.configuration.SoundsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI for The Rift Rotating Event Mode.
 * Shows current event and upcoming event schedule.
 */
public class RotatingEventGUI {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Opens the Rotating Event GUI for the player.
     * 4 rows total:
     * - Row 2: Beacon in the middle (slot 13) - Join The Rift
     * - Row 3: Event schedule (slots 10-16) - 7 paper items showing rotating events
     * - Row 4: Close button
     *
     * @param p The player to show the GUI to
     */
    public static void openRotatingEventGUI(Player p) {
        int size = 36; // 4 rows
        Inventory inv = Bukkit.createInventory(new RotatingEventHolder(), size,
            IridiumColorAPI.process("&b&lThe Rift &8- &6&lRotating Events"));

        // Row 2 - Beacon in the middle (slot 13)
        ItemStack beaconItem = new ItemStack(Material.BEACON, 1);
        ItemMeta beaconMeta = beaconItem.getItemMeta();
        if (beaconMeta != null) {
            beaconMeta.setDisplayName(IridiumColorAPI.process("&a&lJoin The Rift"));
            List<String> beaconLore = new ArrayList<>();
            beaconLore.add(IridiumColorAPI.process("&7"));
            beaconLore.add(IridiumColorAPI.process("&e&lRotating Mode"));
            beaconLore.add(IridiumColorAPI.process("&7Experience different events"));
            beaconLore.add(IridiumColorAPI.process("&7throughout the day!"));
            beaconLore.add(IridiumColorAPI.process("&7"));
            beaconLore.add(IridiumColorAPI.process("&aClick to join a match!"));
            beaconMeta.setLore(beaconLore);
            beaconMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            beaconMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "action"),
                PersistentDataType.STRING, "join-rotating");
            beaconMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"),
                PersistentDataType.STRING, "true");

            beaconItem.setItemMeta(beaconMeta);
        }
        inv.setItem(13, beaconItem);

        // Row 3 - Event schedule (slots 10-16) - 7 slots for rotating events
        RotatingEvent[] events = getRotatingEventSchedule();

        int[] eventSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < events.length && i < eventSlots.length; i++) {
            RotatingEvent event = events[i];
            ItemStack eventItem = createEventItem(event, i == 0);
            inv.setItem(eventSlots[i] + 9, eventItem); // +9 to move from row 2 to row 3
        }

        // Row 4 - Close button (slot 31)
        ItemStack closeItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(IridiumColorAPI.process("&c&lClose"));
            List<String> closeLore = new ArrayList<>();
            closeLore.add(IridiumColorAPI.process("&7Click to close the GUI"));
            closeMeta.setLore(closeLore);

            closeMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "action"),
                PersistentDataType.STRING, "close-gui");
            closeMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"),
                PersistentDataType.STRING, "true");

            closeItem.setItemMeta(closeMeta);
        }
        inv.setItem(31, closeItem);

        p.openInventory(inv);
        SoundsConfig.playSound("arena-selector-open", p);
    }

    /**
     * Creates an item for a rotating event.
     *
     * @param event The rotating event
     * @param isCurrent Whether this is the currently active event
     * @return The ItemStack representing the event
     */
    private static ItemStack createEventItem(RotatingEvent event, boolean isCurrent) {
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Title with different color for current/upcoming
            String title = isCurrent
                ? IridiumColorAPI.process("&a&l● &e" + event.getName() + " &a&l(ACTIVE)")
                : IridiumColorAPI.process("&7&l● &f" + event.getName());

            meta.setDisplayName(title);

            List<String> lore = new ArrayList<>();
            lore.add(IridiumColorAPI.process("&7"));

            // Event description
            for (String line : event.getDescription()) {
                lore.add(IridiumColorAPI.process("&7" + line));
            }

            lore.add(IridiumColorAPI.process("&7"));

            // Time information
            if (isCurrent) {
                lore.add(IridiumColorAPI.process("&a&l▶ &aNOW ACTIVE"));
                lore.add(IridiumColorAPI.process("&7Ends at: &e" + event.getEndTime().format(TIME_FORMATTER)));
            } else {
                lore.add(IridiumColorAPI.process("&e⏰ &6Starts at: &e" + event.getStartTime().format(TIME_FORMATTER)));
                lore.add(IridiumColorAPI.process("&7Ends at: &e" + event.getEndTime().format(TIME_FORMATTER)));
            }

            lore.add(IridiumColorAPI.process("&7Duration: &f" + event.getDurationMinutes() + " minutes"));
            lore.add(IridiumColorAPI.process("&7"));

            // Event features/modifiers
            if (event.getFeatures().length > 0) {
                lore.add(IridiumColorAPI.process("&d&lEvent Features:"));
                for (String feature : event.getFeatures()) {
                    lore.add(IridiumColorAPI.process("&7• &d" + feature));
                }
            }

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            meta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"),
                PersistentDataType.STRING, "true");

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Gets the rotating event schedule for the current day.
     * This is a mock implementation - in production this would come from Redis or config.
     *
     * @return Array of RotatingEvents
     */
    private static RotatingEvent[] getRotatingEventSchedule() {
        LocalDateTime now = LocalDateTime.now();

        // Create a realistic rotating schedule
        // Each event lasts 2-4 hours

        RotatingEvent[] schedule = new RotatingEvent[7];

        // Event 1: Meteorite Storm (00:00 - 03:00)
        schedule[0] = new RotatingEvent(
            "Meteorite Storm",
            now.withHour(0).withMinute(0),
            now.withHour(3).withMinute(0),
            new String[]{
                "Meteorites rain from the sky!",
                "Collect rare &6Meteorite Fragments",
                "Random explosions around the map"
            },
            new String[]{
                "2x Meteorite Shard drops",
                "Random meteorite impacts",
                "Bonus resources from space rocks"
            }
        );

        // Event 2: Gravity Shift (03:00 - 06:00)
        schedule[1] = new RotatingEvent(
            "Gravity Shift",
            now.withHour(3).withMinute(0),
            now.withHour(6).withMinute(0),
            new String[]{
                "Experience altered gravity!",
                "Jump higher, fall slower",
                "New movement strategies"
            },
            new String[]{
                "Low gravity zones",
                "Enhanced jump boost",
                "Slow falling effect"
            }
        );

        // Event 3: Cosmic Chaos (06:00 - 10:00)
        schedule[2] = new RotatingEvent(
            "Cosmic Chaos",
            now.withHour(6).withMinute(0),
            now.withHour(10).withMinute(0),
            new String[]{
                "Random events every 5 minutes!",
                "Expect the unexpected",
                "Double the rewards"
            },
            new String[]{
                "Random buffs and debuffs",
                "Mystery supply drops",
                "2x Crystal rewards"
            }
        );

        // Event 4: Solar Flare (10:00 - 13:00)
        schedule[3] = new RotatingEvent(
            "Solar Flare",
            now.withHour(10).withMinute(0),
            now.withHour(13).withMinute(0),
            new String[]{
                "The sun unleashes its fury!",
                "Fire damage in open areas",
                "Seek shelter or burn"
            },
            new String[]{
                "Periodic fire damage",
                "Safe zones near Rift Cores",
                "Fire resistance upgrades available"
            }
        );

        // Event 5: Nebula Night (13:00 - 17:00)
        schedule[4] = new RotatingEvent(
            "Nebula Night",
            now.withHour(13).withMinute(0),
            now.withHour(17).withMinute(0),
            new String[]{
                "Darkness falls across The Rift",
                "Limited visibility",
                "Night vision becomes essential"
            },
            new String[]{
                "Permanent night time",
                "Glowing ore deposits",
                "Stealth gameplay bonus"
            }
        );

        // Event 6: Asteroid Belt (17:00 - 20:00)
        schedule[5] = new RotatingEvent(
            "Asteroid Belt",
            now.withHour(17).withMinute(0),
            now.withHour(20).withMinute(0),
            new String[]{
                "Navigate through asteroids!",
                "Floating islands appear",
                "New pathways to enemy bases"
            },
            new String[]{
                "Floating asteroid platforms",
                "Parkour challenges",
                "Bonus loot on asteroids"
            }
        );

        // Event 7: Warp Speed (20:00 - 24:00)
        schedule[6] = new RotatingEvent(
            "Warp Speed",
            now.withHour(20).withMinute(0),
            now.withHour(23).withMinute(59),
            new String[]{
                "Everything moves faster!",
                "Speed boost for all players",
                "Fast-paced action"
            },
            new String[]{
                "Permanent Speed III",
                "Faster mining & crafting",
                "Quick match mode"
            }
        );

        return schedule;
    }

    /**
     * Holder class for the Rotating Event GUI inventory.
     */
    public static class RotatingEventHolder implements InventoryHolder {
        @Nullable
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    /**
     * Represents a rotating event in The Rift.
     */
    private static class RotatingEvent {
        private final String name;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final String[] description;
        private final String[] features;

        public RotatingEvent(String name, LocalDateTime startTime, LocalDateTime endTime,
                           String[] description, String[] features) {
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.description = description;
            this.features = features;
        }

        public String getName() {
            return name;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public String[] getDescription() {
            return description;
        }

        public String[] getFeatures() {
            return features;
        }

        public long getDurationMinutes() {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
    }
}

