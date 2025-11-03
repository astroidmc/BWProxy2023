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
     * Opens the Rotating Event GUI for the player.
     * 6 rows total:
     * - Row 1: Current event display
     * - Row 2: Join button (beacon in the middle, slot 13)
     * - Rows 3-5: All available events (7 events)
     * - Row 6: Close button
     *
     * @param p The player to show the GUI to
     */
    public static void openRotatingEventGUI(Player p) {
        int size = 54; // 6 rows
        Inventory inv = Bukkit.createInventory(new RotatingEventHolder(), size,
            IridiumColorAPI.process("&b&lThe Rift &8- &6&lDaily Events"));

        DailyEvent currentEvent = DailyEventManager.getInstance().getCurrentEvent();
        List<DailyEvent> allEvents = DailyEventManager.getInstance().getAllEvents();

        // Row 1 - Current Event Display (slot 4 - center of row 1)
        ItemStack currentEventItem = createCurrentEventDisplay(currentEvent);
        inv.setItem(4, currentEventItem);

        // Row 2 - Beacon in the middle (slot 13)
        ItemStack beaconItem = new ItemStack(Material.BEACON, 1);
        ItemMeta beaconMeta = beaconItem.getItemMeta();
        if (beaconMeta != null) {
            beaconMeta.setDisplayName(IridiumColorAPI.process("&a&lJoin The Rift"));
            List<String> beaconLore = new ArrayList<>();
            beaconLore.add(IridiumColorAPI.process("&7"));
            beaconLore.add(IridiumColorAPI.process("&e&lToday's Event: &f" + currentEvent.getName()));
            beaconLore.add(IridiumColorAPI.process("&7Experience the daily rotating event!"));
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

        // Rows 3-5 - All available events (slots 19-25 for row 3, 28-34 for row 4)
        int[] eventSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < allEvents.size() && i < eventSlots.length; i++) {
            DailyEvent event = allEvents.get(i);
            boolean isCurrent = event.getId().equals(currentEvent.getId());
            ItemStack eventItem = createEventItem(event, isCurrent);
            inv.setItem(eventSlots[i], eventItem);
        }

        // Row 6 - Close button (slot 49 - center of last row)
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
        inv.setItem(49, closeItem);

        p.openInventory(inv);
        SoundsConfig.playSound("arena-selector-open", p);
    }

    /**
     * Creates the current event display item (large showcase).
     *
     * @param event The current daily event
     * @return The ItemStack representing the current event
     */
    private static ItemStack createCurrentEventDisplay(DailyEvent event) {
        ItemStack item = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(IridiumColorAPI.process("&6&l⚡ TODAY'S EVENT ⚡"));

            List<String> lore = new ArrayList<>();
            lore.add(IridiumColorAPI.process("&7"));
            lore.add(IridiumColorAPI.process("&e&l" + event.getName()));
            lore.add(IridiumColorAPI.process("&7"));

            // Event description
            for (String line : event.getDescription()) {
                lore.add(IridiumColorAPI.process("&7" + line));
            }

            lore.add(IridiumColorAPI.process("&7"));
            lore.add(IridiumColorAPI.process("&a&l✦ ACTIVE ALL DAY"));
            lore.add(IridiumColorAPI.process("&7This event is active for the entire day!"));
            lore.add(IridiumColorAPI.process("&7New event at midnight."));
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
            // Title with different color for current/other
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

            // Status information
            if (isCurrent) {
                lore.add(IridiumColorAPI.process("&a&l▶ ACTIVE NOW"));
                lore.add(IridiumColorAPI.process("&7This is today's event!"));
            } else {
                lore.add(IridiumColorAPI.process("&e⏰ &6Coming Soon"));
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
}

