package com.tomkeuper.bedwars.proxy.arenamanager;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import com.tomkeuper.bedwars.proxy.api.ArenaStatus;
import com.tomkeuper.bedwars.proxy.api.CachedArena;
import com.tomkeuper.bedwars.proxy.configuration.ConfigPath;
import com.tomkeuper.bedwars.proxy.configuration.SoundsConfig;
import com.tomkeuper.bedwars.proxy.language.Language;
import com.tomkeuper.bedwars.proxy.api.Messages;
import com.tomkeuper.bedwars.proxy.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaGUI {

    private static final YamlConfiguration yml = BedWarsProxy.config.getYml();


    /**
     * Opens the main GUI with quick join and map view options.
     * Creates a clean 4-row GUI with a bed (slot 11), recovery compass (slot 15), and close barrier (slot 31).
     *
     * @param p The player to show the GUI to
     * @param group The arena group/mode
     */
    public static void openGui(Player p, String group) {
        // Fixed size: 4 rows (36 slots)
        int size = 36;
        Inventory inv = Bukkit.createInventory(new SelectorHolder(), size, Language.getMsg(p, Messages.ARENA_GUI_INV_NAME));

        // Add beacon item on middle row, left-center (slot 11)
        ItemStack bedItem = new ItemStack(Material.BEACON, 1);
        ItemMeta bedMeta = bedItem.getItemMeta();
        if (bedMeta != null) {
            bedMeta.setDisplayName(IridiumColorAPI.process(ChatColor.GREEN + "The Rift (" + group + ")"));
            List<String> bedLore = new ArrayList<>();
            bedLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Click to jump into a random arena"));
            bedLore.add(IridiumColorAPI.process(ChatColor.GRAY + "and start your battle!"));
            bedMeta.setLore(bedLore);

            bedMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "quick-join");
            bedMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING, group);
            bedMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"), PersistentDataType.STRING, "true");

            bedItem.setItemMeta(bedMeta);
        }
        inv.setItem(11, bedItem);

        // Add empty map with information about The Rift/BedWars on middle row, center (slot 13)
        ItemStack infoItem;
        try {
            infoItem = new ItemStack(Material.valueOf("MAP"), 1);
        } catch (Exception e) {
            infoItem = new ItemStack(Material.PAPER, 1);
        }
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(IridiumColorAPI.process(ChatColor.YELLOW + "What is The Rift?"));
            List<String> infoLore = new ArrayList<>();
            infoLore.add(IridiumColorAPI.process("&7AstroidMC's take on BedWars — reimagined among the stars."));
            infoLore.add(IridiumColorAPI.process("&7Defend your &bRift Core &7and shatter others across the void."));
            infoLore.add(IridiumColorAPI.process("&7Gather &6Meteorite Shards &7and &bIons &7to upgrade your gear."));
            infoLore.add(IridiumColorAPI.process("&7Use &dCosmic Crystals &7to unlock powerful gadgets and shields."));
            infoMeta.setLore(infoLore);

            infoMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "info");
            infoMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"), PersistentDataType.STRING, "true");

            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(13, infoItem);

        // Add recovery compass/paper on middle row, right-center (slot 15)
        ItemStack mapListItem;
        try {
            // Try recovery compass first (1.19+)
            mapListItem = new ItemStack(Material.valueOf("RECOVERY_COMPASS"), 1);
        } catch (Exception e) {
            // Fallback to paper for older versions
            mapListItem = new ItemStack(Material.PAPER, 1);
        }
        ItemMeta mapMeta = mapListItem.getItemMeta();
        if (mapMeta != null) {
            mapMeta.setDisplayName(IridiumColorAPI.process(ChatColor.AQUA + "View All Maps"));
            List<String> mapLore = new ArrayList<>();
            mapLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Click to see all available maps"));
            mapLore.add(IridiumColorAPI.process(ChatColor.GRAY + "for this mode"));
            mapMeta.setLore(mapLore);

            mapMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "view-maps");
            mapMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING, group);
            mapMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"), PersistentDataType.STRING, "true");

            mapListItem.setItemMeta(mapMeta);
        }
        inv.setItem(15, mapListItem);

        // Add barrier on bottom row, center (slot 31) to close GUI
        ItemStack closeItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(IridiumColorAPI.process(ChatColor.RED + "Close"));
            List<String> closeLore = new ArrayList<>();
            closeLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Click to close the GUI"));
            closeMeta.setLore(closeLore);

            closeMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "close-gui");
            closeMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"), PersistentDataType.STRING, "true");

            closeItem.setItemMeta(closeMeta);
        }
        inv.setItem(31, closeItem);

        p.openInventory(inv);
        SoundsConfig.playSound("arena-selector-open", p);
    }

    /**
     * Opens a GUI showing all available maps for the specified group.
     * Displays each arena as a separate item with detailed information.
     * Now with pagination - 4 rows (28 arena slots) per page.
     *
     * @param p The player to show the GUI to
     * @param group The arena group/mode to display maps for
     */
    public static void openMapsView(Player p, String group) {
        openMapsView(p, group, 0);
    }

    /**
     * Opens a paginated GUI showing all available maps for the specified group.
     * 4 rows = 36 slots total
     * - Rows 1-3 (slots 0-26, excluding bottom row): Arena items (27 slots)
     * - Row 4 (bottom row): Navigation and info items
     *
     * @param p The player to show the GUI to
     * @param group The arena group/mode to display maps for
     * @param page The page number (0-indexed)
     */
    public static void openMapsView(Player p, String group, int page) {
        // Get all arenas for this group
        List<CachedArena> allArenas;
        if (group.equalsIgnoreCase("default")) {
            allArenas = new ArrayList<>(ArenaManager.getArenas());
        } else {
            allArenas = new ArrayList<>();
            for (CachedArena a : ArenaManager.getArenas()){
                if (a.getArenaGroup().equalsIgnoreCase(group)) allArenas.add(a);
            }
        }

        // Remove playing arenas if configured
        allArenas.removeIf(a -> a.getStatus() == ArenaStatus.PLAYING && !BedWarsProxy.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_SETTINGS_SHOW_PLAYING));

        com.tomkeuper.bedwars.proxy.api.Language lang = LanguageManager.get().getPlayerLanguage(p);

        // Group arenas by map name (multiple docker containers can have same map)
        // Use LinkedHashMap to maintain insertion order
        java.util.Map<String, List<CachedArena>> arenasByName = new java.util.LinkedHashMap<>();
        for (CachedArena arena : allArenas) {
            String mapName = arena.getDisplayName(lang);
            arenasByName.computeIfAbsent(mapName, k -> new ArrayList<>()).add(arena);
        }

        // For each unique map name, pick the best representative (most players)
        // This will be shown in the GUI, but clicking will search all instances
        List<CachedArena> uniqueArenas = new ArrayList<>();
        for (java.util.Map.Entry<String, List<CachedArena>> entry : arenasByName.entrySet()) {
            List<CachedArena> instances = entry.getValue();
            // Sort by player count (descending) and pick the fullest one as representative
            CachedArena best = instances.stream()
                .sorted((a1, a2) -> Integer.compare(a2.getCurrentPlayers(), a1.getCurrentPlayers()))
                .findFirst()
                .orElse(instances.get(0));
            uniqueArenas.add(best);
        }

        // Sort unique arenas
        List<CachedArena> arenas = uniqueArenas.stream().sorted(ArenaManager.getComparator()).collect(Collectors.toList());

        // Pagination setup: 4 rows = 36 slots, use first 3 rows (27 slots) for arenas
        int arenaSlotsPerPage = 27;
        int totalPages = (int) Math.ceil((double) arenas.size() / arenaSlotsPerPage);
        if (totalPages == 0) totalPages = 1;

        // Validate page number
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        // Create 4-row inventory
        Inventory inv = Bukkit.createInventory(new MapViewHolder(group, page), 36,
            IridiumColorAPI.process(ChatColor.AQUA + "Maps - " + group + " &7(Page " + (page + 1) + "/" + totalPages + ")"));

        // Calculate start and end indices for this page
        int startIndex = page * arenaSlotsPerPage;
        int endIndex = Math.min(startIndex + arenaSlotsPerPage, arenas.size());

        // Add arena items to first 3 rows (slots 0-26)
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            CachedArena ca = arenas.get(i);
            String mapName = ca.getDisplayName(lang);

            // Get all instances of this map to calculate total players
            List<CachedArena> mapInstances = arenasByName.get(mapName);
            int totalPlayers = mapInstances.stream().mapToInt(CachedArena::getCurrentPlayers).sum();
            int totalMaxPlayers = mapInstances.stream().mapToInt(CachedArena::getMaxPlayers).sum();

            String status;
            switch (ca.getStatus()) {
                case WAITING:
                    status = "waiting";
                    break;
                case PLAYING:
                    status = "playing";
                    break;
                case STARTING:
                    status = "starting";
                    break;
                default:
                    continue;
            }

            ItemStack item = new ItemStack(Material.valueOf(yml.getString(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_MATERIAL.replace("%path%", status))
            ), 1, (byte) yml.getInt(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_DATA.replace("%path%", status)));
            if (item == null) item = new ItemStack(Material.BEDROCK);

            if (yml.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_ENCHANTED.replace("%path%", status))) {
                if (item.getItemMeta() != null){
                    ItemMeta im = item.getItemMeta();
                    im.addEnchant(Enchantment.LURE, 1, true);
                    im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(im);
                }
            }

            ItemMeta im = item.getItemMeta();
            if (im != null){
                im.setDisplayName(Language.getMsg(p, Messages.ARENA_GUI_ARENA_CONTENT_NAME).replace("{name}", mapName));
                List<String> lore = new ArrayList<>();
                for (String s : LanguageManager.get().getList(p, Messages.ARENA_GUI_ARENA_CONTENT_LORE)) {
                    if (!(s.contains("{group}") && ca.getArenaGroup().equalsIgnoreCase("default"))) {
                        lore.add(s.replace("{on}", String.valueOf(totalPlayers)).replace("{max}",
                                String.valueOf(totalMaxPlayers)).replace("{status}", ca.getDisplayStatus(lang))
                                .replace("{group}", ca.getDisplayGroup(lang)));
                    }
                }
                im.setLore(lore);

                // Store map name instead of server/identifier for grouped lookup
                im.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "map_name"), PersistentDataType.STRING, mapName);
                im.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING, ca.getArenaGroup());
                im.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "cancelClick"), PersistentDataType.STRING, "true");

                item.setItemMeta(im);
            }

            inv.setItem(slot, item);
            slot++;
        }

        // Add navigation items to bottom row (row 4, slots 27-35)

        // Previous page button (slot 27) - only if not on first page
        if (page > 0) {
            ItemStack prevItem = new ItemStack(Material.ARROW, 1);
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(IridiumColorAPI.process(ChatColor.YELLOW + "← Previous Page"));
                List<String> prevLore = new ArrayList<>();
                prevLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Go to page " + page));
                prevMeta.setLore(prevLore);
                prevMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "prev-page");
                prevMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING, group);
                prevMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "page"), PersistentDataType.INTEGER, page - 1);
                prevItem.setItemMeta(prevMeta);
            }
            inv.setItem(27, prevItem);
        }

        // Info item (slot 31) - center of bottom row
        ItemStack infoItem = new ItemStack(Material.PAPER, 1);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(IridiumColorAPI.process(ChatColor.GOLD + "Page Info"));
            List<String> infoLore = new ArrayList<>();
            infoLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Page: " + ChatColor.YELLOW + (page + 1) + "/" + totalPages));
            infoLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Total Maps: " + ChatColor.YELLOW + arenas.size()));
            infoLore.add("");
            infoLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Left-Click to join"));
            infoLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Right-Click to spectate"));
            infoMeta.setLore(infoLore);
            infoMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "page-info");
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(31, infoItem);

        // Next page button (slot 35) - only if not on last page
        if (page < totalPages - 1) {
            ItemStack nextItem = new ItemStack(Material.ARROW, 1);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(IridiumColorAPI.process(ChatColor.YELLOW + "Next Page →"));
                List<String> nextLore = new ArrayList<>();
                nextLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Go to page " + (page + 2)));
                nextMeta.setLore(nextLore);
                nextMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "next-page");
                nextMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING, group);
                nextMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "page"), PersistentDataType.INTEGER, page + 1);
                nextItem.setItemMeta(nextMeta);
            }
            inv.setItem(35, nextItem);
        }

        // Back button (slot 29) - return to main selector
        ItemStack backItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(IridiumColorAPI.process(ChatColor.RED + "← Back"));
            List<String> backLore = new ArrayList<>();
            backLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Return to main menu"));
            backMeta.setLore(backLore);
            backMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING, "back-to-main");
            backMeta.getPersistentDataContainer().set(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING, group);
            backItem.setItemMeta(backMeta);
        }
        inv.setItem(29, backItem);

        p.openInventory(inv);
        SoundsConfig.playSound("arena-selector-open", p);
    }


    public static class SelectorHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class MapViewHolder implements InventoryHolder {
        private final String group;
        private final int page;

        public MapViewHolder(String group, int page) {
            this.group = group;
            this.page = page;
        }

        public String getGroup() {
            return group;
        }

        public int getPage() {
            return page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
