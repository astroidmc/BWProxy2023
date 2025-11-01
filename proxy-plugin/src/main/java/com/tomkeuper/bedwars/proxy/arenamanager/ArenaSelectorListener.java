package com.tomkeuper.bedwars.proxy.arenamanager;

import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import com.astroid.bedwars.proxy.api.ArenaStatus;
import com.astroid.bedwars.proxy.api.CachedArena;
import com.tomkeuper.bedwars.proxy.configuration.SoundsConfig;
import com.tomkeuper.bedwars.proxy.language.Language;
import com.astroid.bedwars.proxy.api.Messages;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ArenaSelectorListener implements Listener {

    @EventHandler
    public void onArenaSelectorClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof ArenaGUI.SelectorHolder) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack i = e.getCurrentItem();

            if (i == null) return;
            if (i.getType() == Material.AIR) return;

            ItemMeta meta = i.getItemMeta();
            if (meta == null) return;
            
            String action = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING);

            // Handle quick join
            if ("quick-join".equals(action)) {
                String group = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING);
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

                // Find the arena with the most players (smart matchmaking)
                // This fills up arenas faster and creates better games
                CachedArena bestArena = null;
                int maxPlayers = -1;

                for (CachedArena arena : availableArenas) {
                    int currentPlayers = arena.getCurrentPlayers();
                    // Prioritize arenas with more players
                    if (currentPlayers > maxPlayers) {
                        maxPlayers = currentPlayers;
                        bestArena = arena;
                    }
                }

                // Try to join the fullest arena
                if (bestArena != null && bestArena.addPlayer(p, null)) {
                    SoundsConfig.playSound("join-allowed", p);
                } else {
                    // If the fullest arena is full, try other arenas
                    boolean joined = false;
                    for (CachedArena arena : availableArenas) {
                        if (arena != bestArena && arena.addPlayer(p, null)) {
                            SoundsConfig.playSound("join-allowed", p);
                            joined = true;
                            break;
                        }
                    }

                    if (!joined) {
                        SoundsConfig.playSound("join-denied", p);
                        p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                    }
                }
                p.closeInventory();
                return;
            }

            // Handle view maps
            if ("view-maps".equals(action)) {
                String group = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING);
                if (group == null) group = "default";
                ArenaGUI.openMapsView(p, group);
                return;
            }

            // Handle close GUI
            if ("close-gui".equals(action)) {
                p.closeInventory();
                return;
            }

            // Handle feedback
            if ("feedback".equals(action)) {
                com.tomkeuper.bedwars.proxy.utils.FeedbackManager.getInstance().promptFeedback(p);
                return;
            }

            // Handle regular arena click
            String server = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "server"), PersistentDataType.STRING);
            String identifier = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "world_identifier"), PersistentDataType.STRING);

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

            ItemMeta meta = i.getItemMeta();
            if (meta == null) return;
            
            String action = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING);

            // Handle pagination actions
            if ("next-page".equals(action)) {
                String group = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING);
                Integer page = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "page"), PersistentDataType.INTEGER);
                if (group != null && page != null) {
                    ArenaGUI.openMapsView(p, group, page);
                    SoundsConfig.playSound("arena-selector-click", p);
                }
                return;
            }

            if ("prev-page".equals(action)) {
                String group = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING);
                Integer page = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "page"), PersistentDataType.INTEGER);
                if (group != null && page != null) {
                    ArenaGUI.openMapsView(p, group, page);
                    SoundsConfig.playSound("arena-selector-click", p);
                }
                return;
            }

            if ("back-to-main".equals(action)) {
                String group = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING);
                if (group != null) {
                    ArenaGUI.openGui(p, group);
                    SoundsConfig.playSound("arena-selector-click", p);
                }
                return;
            }

            if ("page-info".equals(action)) {
                // Just info, do nothing
                return;
            }


            // Handle regular arena click - check for grouped map first
            String mapName = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "map_name"), PersistentDataType.STRING);
            String arenaGroup = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "group"), PersistentDataType.STRING);

            if (mapName != null && arenaGroup != null) {
                // Find all arena instances with this map name (multiple docker containers)
                com.astroid.bedwars.proxy.api.Language lang = com.tomkeuper.bedwars.proxy.language.LanguageManager.get().getPlayerLanguage(p);
                java.util.List<CachedArena> mapInstances = new java.util.ArrayList<>();

                for (CachedArena arena : ArenaManager.getArenas()) {
                    if (arena.getArenaGroup().equalsIgnoreCase(arenaGroup) &&
                        arena.getDisplayName(lang).equals(mapName)) {
                        mapInstances.add(arena);
                    }
                }

                if (mapInstances.isEmpty()) {
                    p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                    SoundsConfig.playSound("join-denied", p);
                    p.closeInventory();
                    return;
                }

                if (e.getClick() == ClickType.LEFT) {
                    // Join: find the instance with most players (smart matchmaking)
                    java.util.List<CachedArena> joinableArenas = new java.util.ArrayList<>();
                    for (CachedArena arena : mapInstances) {
                        if (arena.getStatus() == ArenaStatus.WAITING || arena.getStatus() == ArenaStatus.STARTING) {
                            joinableArenas.add(arena);
                        }
                    }

                    if (joinableArenas.isEmpty()) {
                        p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                        SoundsConfig.playSound("join-denied", p);
                        p.closeInventory();
                        return;
                    }

                    // Sort by most players first (fullest arena = faster game start)
                    joinableArenas.sort((a1, a2) -> Integer.compare(a2.getCurrentPlayers(), a1.getCurrentPlayers()));

                    // Try to join, starting with the fullest
                    boolean joined = false;
                    for (CachedArena arena : joinableArenas) {
                        if (arena.addPlayer(p, null)) {
                            SoundsConfig.playSound("join-allowed", p);
                            joined = true;
                            break;
                        }
                    }

                    if (!joined) {
                        SoundsConfig.playSound("join-denied", p);
                        p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                    }

                } else if (e.getClick() == ClickType.RIGHT) {
                    // Spectate: find any playing instance
                    boolean spectating = false;
                    for (CachedArena arena : mapInstances) {
                        if (arena.getStatus() == ArenaStatus.PLAYING && arena.addSpectator(p, null)) {
                            SoundsConfig.playSound("spectate-allowed", p);
                            spectating = true;
                            break;
                        }
                    }

                    if (!spectating) {
                        p.sendMessage(Language.getMsg(p, Messages.ARENA_SPECTATE_DENIED_SELECTOR));
                        SoundsConfig.playSound("spectate-denied", p);
                    }
                }
                p.closeInventory();
                return;
            }

            // Fallback for old format (backwards compatibility with old arena items)
            String server = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "server"), PersistentDataType.STRING);
            String identifier = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "world_identifier"), PersistentDataType.STRING);

            if (server != null && identifier != null) {
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

        // Handle Rotating Event GUI clicks
        if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof RotatingEventGUI.RotatingEventHolder) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack i = e.getCurrentItem();

            if (i == null) return;
            if (i.getType() == Material.AIR) return;

            ItemMeta meta = i.getItemMeta();
            if (meta == null) return;

            String action = meta.getPersistentDataContainer().get(new NamespacedKey(BedWarsProxy.getPlugin(), "action"), PersistentDataType.STRING);

            // Handle join rotating mode
            if ("join-rotating".equals(action)) {
                // Find all arenas in the "rotating" group
                java.util.List<CachedArena> availableArenas = new java.util.ArrayList<>();
                for (CachedArena arena : ArenaManager.getArenas()) {
                    if (arena.getArenaGroup().equalsIgnoreCase("rotating")) {
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

                // Smart matchmaking: find the arena with the most players
                CachedArena bestArena = null;
                int maxPlayers = -1;

                for (CachedArena arena : availableArenas) {
                    int currentPlayers = arena.getCurrentPlayers();
                    if (currentPlayers > maxPlayers) {
                        maxPlayers = currentPlayers;
                        bestArena = arena;
                    }
                }

                // Try to join the best arena
                if (bestArena != null && bestArena.addPlayer(p, null)) {
                    SoundsConfig.playSound("join-allowed", p);
                } else {
                    // If the best arena is full, try others
                    boolean joined = false;
                    for (CachedArena arena : availableArenas) {
                        if (arena != bestArena && arena.addPlayer(p, null)) {
                            SoundsConfig.playSound("join-allowed", p);
                            joined = true;
                            break;
                        }
                    }

                    if (!joined) {
                        SoundsConfig.playSound("join-denied", p);
                        p.sendMessage(Language.getMsg(p, Messages.ARENA_JOIN_DENIED_SELECTOR));
                    }
                }
                p.closeInventory();
                return;
            }

            // Handle close GUI
            if ("close-gui".equals(action)) {
                p.closeInventory();
            }
        }
    }
}
