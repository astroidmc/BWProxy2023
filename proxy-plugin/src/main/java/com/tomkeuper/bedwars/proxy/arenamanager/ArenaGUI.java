package com.tomkeuper.bedwars.proxy.arenamanager;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.saicone.rtag.RtagItem;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            bedItem.setItemMeta(bedMeta);
        }
        bedItem = RtagItem.edit(bedItem, tag -> {
            tag.set("quick-join", "action");
            tag.set(group, "group");
            tag.set("true", "cancelClick");
        });
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
            infoItem.setItemMeta(infoMeta);
        }
        infoItem = RtagItem.edit(infoItem, tag -> {
            tag.set("info", "action");
            tag.set("true", "cancelClick");
        });
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
            mapListItem.setItemMeta(mapMeta);
        }
        mapListItem = RtagItem.edit(mapListItem, tag -> {
            tag.set("view-maps", "action");
            tag.set(group, "group");
            tag.set("true", "cancelClick");
        });
        inv.setItem(15, mapListItem);

        // Add barrier on bottom row, center (slot 31) to close GUI
        ItemStack closeItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(IridiumColorAPI.process(ChatColor.RED + "Close"));
            List<String> closeLore = new ArrayList<>();
            closeLore.add(IridiumColorAPI.process(ChatColor.GRAY + "Click to close the GUI"));
            closeMeta.setLore(closeLore);
            closeItem.setItemMeta(closeMeta);
        }
        closeItem = RtagItem.edit(closeItem, tag -> {
            tag.set("close-gui", "action");
            tag.set("true", "cancelClick");
        });
        inv.setItem(31, closeItem);

        p.openInventory(inv);
        SoundsConfig.playSound("arena-selector-open", p);
    }

    /**
     * Opens a GUI showing all available maps for the specified group.
     * Displays each arena as a separate item with detailed information.
     *
     * @param p The player to show the GUI to
     * @param group The arena group/mode to display maps for
     */
    public static void openMapsView(Player p, String group) {
        // Get all arenas for this group
        List<CachedArena> arenas;
        if (group.equalsIgnoreCase("default")) {
            arenas = new ArrayList<>(ArenaManager.getArenas());
        } else {
            arenas = new ArrayList<>();
            for (CachedArena a : ArenaManager.getArenas()){
                if (a.getArenaGroup().equalsIgnoreCase(group)) arenas.add(a);
            }
        }

        // Remove playing arenas if configured
        arenas.removeIf(a -> a.getStatus() == ArenaStatus.PLAYING && !BedWarsProxy.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_SETTINGS_SHOW_PLAYING));

        // Sort arenas
        arenas = arenas.stream().sorted(ArenaManager.getComparator()).collect(Collectors.toList());

        // Calculate inventory size (minimum 27, rounds up to nearest 9)
        int size = Math.min(54, Math.max(27, ((arenas.size() + 8) / 9) * 9));

        Inventory inv = Bukkit.createInventory(new MapViewHolder(), size, ChatColor.AQUA + "Available Maps - " + group);

        int slot = 0;
        for (CachedArena ca : arenas) {
            if (slot >= size) break;

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

            ItemStack i = new ItemStack(Material.valueOf(yml.getString(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_MATERIAL.replace("%path%", status))
            ), 1, (byte) yml.getInt(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_DATA.replace("%path%", status)));
            if (i == null) i = new ItemStack(Material.BEDROCK);

            if (yml.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_ENCHANTED.replace("%path%", status))) {
                if (i.getItemMeta() != null){
                    ItemMeta im = i.getItemMeta();
                    im.addEnchant(Enchantment.LURE, 1, true);
                    im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    i.setItemMeta(im);
                }
            }

            ItemMeta im = i.getItemMeta();
            com.tomkeuper.bedwars.proxy.api.Language lang = LanguageManager.get().getPlayerLanguage(p);
            if (im != null){
                im.setDisplayName(Language.getMsg(p, Messages.ARENA_GUI_ARENA_CONTENT_NAME).replace("{name}", ca.getDisplayName(lang)));
                List<String> lore = new ArrayList<>();
                for (String s : LanguageManager.get().getList(p, Messages.ARENA_GUI_ARENA_CONTENT_LORE)) {
                    if (!(s.contains("{group}") && ca.getArenaGroup().equalsIgnoreCase("default"))) {
                        lore.add(s.replace("{on}", String.valueOf(ca.getCurrentPlayers())).replace("{max}",
                                String.valueOf(ca.getMaxPlayers())).replace("{status}", ca.getDisplayStatus(lang))
                                .replace("{group}", ca.getDisplayGroup(lang)));
                    }
                }
                im.setLore(lore);
                i.setItemMeta(im);
            }

            i = RtagItem.edit(i, tag -> {
                tag.set(ca.getServer(), "server");
                tag.set(ca.getRemoteIdentifier(), "world_identifier");
                tag.set("true", "cancelClick");
            });

            inv.setItem(slot, i);
            slot++;
        }

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
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
