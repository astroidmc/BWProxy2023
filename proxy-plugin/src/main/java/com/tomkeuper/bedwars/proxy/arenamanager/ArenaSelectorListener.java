package com.tomkeuper.bedwars.proxy.arenamanager;

import com.saicone.rtag.RtagItem;
import com.tomkeuper.bedwars.proxy.api.ArenaStatus;
import com.tomkeuper.bedwars.proxy.api.CachedArena;
import com.tomkeuper.bedwars.proxy.configuration.SoundsConfig;
import com.tomkeuper.bedwars.proxy.language.Language;
import com.tomkeuper.bedwars.proxy.api.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaSelectorListener implements Listener {

    @EventHandler
    public void onArenaSelectorClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof ArenaGUI.SelectorHolder) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack i = e.getCurrentItem();

            if (i == null) return;
            if (i.getType() == Material.AIR) return;

            RtagItem rtagItem = new RtagItem(i);
            String action = rtagItem.get("action");

            // Handle quick join
            if ("quick-join".equals(action)) {
                String group = rtagItem.get("group");
                if (group == null) group = "default";

                // Get all available arenas for this group
                java.util.List<CachedArena> availableArenas = new java.util.ArrayList<>();
                for (CachedArena arena : ArenaManager.getArenas()) {
                    if (group.equalsIgnoreCase("default") || arena.getArenaGroup().equalsIgnoreCase(group)) {
                        if (arena.getStatus() == ArenaStatus.WAITING || arena.getStatus() == ArenaStatus.STARTING) {
                            availableArenas.add(arena);
                        }
                    }
                }

                if (availableArenas.isEmpty()) {
                    SoundsConfig.playSound("join-denied", p);
                    p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                    p.closeInventory();
                    return;
                }

                // Pick a random arena
                CachedArena randomArena = availableArenas.get(new java.util.Random().nextInt(availableArenas.size()));

                if (randomArena.addPlayer(p, null)) {
                    SoundsConfig.playSound("join-allowed", p);
                } else {
                    SoundsConfig.playSound("join-denied", p);
                    p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                }
                p.closeInventory();
                return;
            }

            // Handle view maps
            if ("view-maps".equals(action)) {
                String group = rtagItem.get("group");
                if (group == null) group = "default";
                ArenaGUI.openMapsView(p, group);
                return;
            }

            // Handle close GUI
            if ("close-gui".equals(action)) {
                p.closeInventory();
                return;
            }

            // Handle regular arena click
            String server = rtagItem.get("server");
            String identifier = rtagItem.get("world_identifier");

            CachedArena a = ArenaManager.getInstance().getArena(server, identifier);
            if (a == null) return;

            if (e.getClick() == ClickType.LEFT) {
                if ((a.getStatus() == ArenaStatus.WAITING || a.getStatus() == ArenaStatus.STARTING) && a.addPlayer(p, null)) {
                    SoundsConfig.playSound("join-allowed", p);
                } else {
                    SoundsConfig.playSound("join-denied", p);
                    p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                }
            } else if (e.getClick() == ClickType.RIGHT) {
                if (a.getStatus() == ArenaStatus.PLAYING && a.addSpectator(p, null)) {
                    SoundsConfig.playSound("spectate-allowed", p);
                } else {
                    p.sendMessage(Language.getMsg(p, Messages.ARENA_SPECTATE_DENIED_SELECTOR));
                    SoundsConfig.playSound("spectate-denied", p);
                }
            }
            p.closeInventory();
        }

        // Handle map view clicks
        if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof ArenaGUI.MapViewHolder) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack i = e.getCurrentItem();

            if (i == null) return;
            if (i.getType() == Material.AIR) return;

            RtagItem rtagItem = new RtagItem(i);
            String server = rtagItem.get("server");
            String identifier = rtagItem.get("world_identifier");

            CachedArena a = ArenaManager.getInstance().getArena(server, identifier);
            if (a == null) return;

            if (e.getClick() == ClickType.LEFT) {
                if ((a.getStatus() == ArenaStatus.WAITING || a.getStatus() == ArenaStatus.STARTING) && a.addPlayer(p, null)) {
                    SoundsConfig.playSound("join-allowed", p);
                } else {
                    SoundsConfig.playSound("join-denied", p);
                    p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                }
            } else if (e.getClick() == ClickType.RIGHT) {
                if (a.getStatus() == ArenaStatus.PLAYING && a.addSpectator(p, null)) {
                    SoundsConfig.playSound("spectate-allowed", p);
                } else {
                    p.sendMessage(Language.getMsg(p, Messages.ARENA_SPECTATE_DENIED_SELECTOR));
                    SoundsConfig.playSound("spectate-denied", p);
                }
            }
            p.closeInventory();
        }
    }


}
