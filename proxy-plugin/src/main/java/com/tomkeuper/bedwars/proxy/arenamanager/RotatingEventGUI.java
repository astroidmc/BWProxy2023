package com.tomkeuper.bedwars.proxy.arenamanager;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import com.tomkeuper.bedwars.proxy.arenamanager.DailyEventManager.DailyEvent;
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

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for The Rift Daily Rotating Event Mode.
 * Shows the current daily event and all available events.
 */
public class RotatingEventGUI {

    /**
     * Opens the main Rotating Event GUI for the player.
     * 5 rows total:
     * - Row 1: Event Calendar (clock in center, slot 4)
     * - Row 3: Join button (beacon in the middle, slot 22)
     * - Row 5: Empty
     *
     * @param p The player to show the GUI to
     */
    public static void openRotatingEventGUI(Player p) {
        int size = 45; // 5 rows
        Inventory inv = Bukkit.createInventory(new RotatingEventHolder(), size,
            IridiumColorAPI.process("&8Daily Events"));

        DailyEvent currentEvent = DailyEventManager.getInstance().getCurrentEvent();

        // Row 1 - Event Calendar (clock in center, slot 4)
        ItemStack clockItem = new ItemStack(Material.CLOCK, 1);
        ItemMeta clockMeta = clockItem.getItemMeta();
        if (clockMeta != null) {
            clockMeta.setDisplayName(IridiumColorAPI.process("&e&lEvent Calendar"));
            List<String> clockLore = new ArrayList<>();
            clockLore.add(IridiumColorAPI.process("&7"));
            clockLore.add(IridiumColorAPI.process("&7View all available daily events"));
            clockLore.add(IridiumColorAPI.process("&7and see what's coming next!"));
            clockLore.add(IridiumColorAPI.process("&7"));
            clockLore.add(IridiumColorAPI.process("&aClick to open the calendar!"));
            clockMeta.setLore(clockLore);
            clockMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            clockMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "action"),
                PersistentDataType.STRING, "open-event-calendar");
            clockMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"),
                PersistentDataType.STRING, "true");

            clockItem.setItemMeta(clockMeta);
        }
        inv.setItem(4, clockItem);

        // Row 3 - Beacon in the middle (slot 22 - middle of row 3 in 5-row GUI)
        ItemStack beaconItem = new ItemStack(Material.BEACON, 1);
        ItemMeta beaconMeta = beaconItem.getItemMeta();
        if (beaconMeta != null) {
            beaconMeta.setDisplayName(IridiumColorAPI.process("&a&lJoin &e&l" + currentEvent.getName()));
            List<String> beaconLore = new ArrayList<>();
            beaconLore.add(IridiumColorAPI.process("&7"));
            beaconLore.add(IridiumColorAPI.process("&e&lToday's Event: &f" + currentEvent.getName()));
            beaconLore.add(IridiumColorAPI.process("&7"));

            // Event description
            for (String line : currentEvent.getDescription()) {
                beaconLore.add(IridiumColorAPI.process("&7" + line));
            }

            beaconLore.add(IridiumColorAPI.process("&7"));

            // Event features
            if (currentEvent.getFeatures().length > 0) {
                beaconLore.add(IridiumColorAPI.process("&d&lEvent Features:"));
                for (String feature : currentEvent.getFeatures()) {
                    beaconLore.add(IridiumColorAPI.process("&7• &d" + feature));
                }
                beaconLore.add(IridiumColorAPI.process("&7"));
            }

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
        inv.setItem(22, beaconItem);

        p.openInventory(inv);
        SoundsConfig.playSound("arena-selector-open", p);
    }

    /**
     * Opens the Event Calendar GUI showing all available events.
     * 4 rows total:
     * - Row 2: All events (slots 10-16, 7 events)
     * - Row 4: Back arrow (slot 31 - center of last row)
     *
     * @param p The player to show the GUI to
     */
    public static void openEventCalendarGUI(Player p) {
        int size = 36; // 4 rows
        Inventory inv = Bukkit.createInventory(new EventCalendarHolder(), size,
            IridiumColorAPI.process("&8Event Calendar"));

        DailyEvent currentEvent = DailyEventManager.getInstance().getCurrentEvent();
        List<DailyEvent> allEvents = DailyEventManager.getInstance().getAllEvents();

        // Row 2 - All events (slots 10-16, starting from 2nd slot to second-to-last slot)
        int[] eventSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < allEvents.size() && i < eventSlots.length; i++) {
            DailyEvent event = allEvents.get(i);
            boolean isCurrent = event.getId().equals(currentEvent.getId());
            ItemStack eventItem = createEventItem(event, isCurrent);
            inv.setItem(eventSlots[i], eventItem);
        }

        // Row 4 - Back arrow (slot 31 - center of last row)
        ItemStack backItem = new ItemStack(Material.ARROW, 1);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(IridiumColorAPI.process("&a&lBack"));
            List<String> backLore = new ArrayList<>();
            backLore.add(IridiumColorAPI.process("&7Return to the main menu"));
            backMeta.setLore(backLore);

            backMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "action"),
                PersistentDataType.STRING, "back-to-main");
            backMeta.getPersistentDataContainer().set(
                new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"),
                PersistentDataType.STRING, "true");

            backItem.setItemMeta(backMeta);
        }
        inv.setItem(31, backItem);

        p.openInventory(inv);
        SoundsConfig.playSound("arena-selector-open", p);
    }


    /**
     * Creates an item for a daily event in the event list.
     *
     * @param event The daily event
     * @param isCurrent Whether this is the currently active event
     * @return The ItemStack representing the event
     */
    private static ItemStack createEventItem(DailyEvent event, boolean isCurrent) {
        Material material = isCurrent ? Material.GLOWSTONE_DUST : Material.PAPER;
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Calculate days until this event
            int daysUntil = DailyEventManager.getInstance().getDaysUntilEvent(event);

            // Title with different color for current/other
            String title;
            if (isCurrent) {
                title = IridiumColorAPI.process("&a&l● &e" + event.getName() + " &a&l(ACTIVE NOW)");
            } else {
                title = IridiumColorAPI.process("&7&l● &f" + event.getName());
            }

            meta.setDisplayName(title);

            List<String> lore = new ArrayList<>();
            lore.add(IridiumColorAPI.process("&7"));

            // Event description
            for (String line : event.getDescription()) {
                lore.add(IridiumColorAPI.process("&7" + line));
            }

            lore.add(IridiumColorAPI.process("&7"));

            // Status information with days until active
            if (isCurrent) {
                lore.add(IridiumColorAPI.process("&a&l▶ ACTIVE NOW"));
                lore.add(IridiumColorAPI.process("&7This is today's event!"));
            } else {
                String daysText;
                if (daysUntil == 1) {
                    daysText = "&e⏰ &6Active in 1 day";
                } else {
                    daysText = "&e⏰ &6Active in " + daysUntil + " days";
                }
                lore.add(IridiumColorAPI.process(daysText));
                lore.add(IridiumColorAPI.process("&7This event will be active"));
                lore.add(IridiumColorAPI.process("&7on a future day."));
            }

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
     * Holder class for the Event Calendar GUI inventory.
     */
    public static class EventCalendarHolder implements InventoryHolder {
        @Nullable
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}

