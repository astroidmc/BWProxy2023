# Arena Plugin Integration Guide - Daily Rotating Event System

## Overview
Dit document beschrijft hoe je het Daily Rotating Event System van de proxy plugin integreert met je BedWars arena plugin (de plugin die de games zelf runt).

## Architectuur Overzicht

```
┌─────────────────────────────────────────────────────────────┐
│                      PROXY PLUGIN                            │
│  - DailyEventManager (beheert welk event actief is)         │
│  - RotatingEventGUI (laat spelers joinen)                   │
│  - Redis Storage (slaat event ID op)                        │
│  - PlaceholderAPI (placeholders voor current event)         │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      │ Redis Communication
                      │ (event_id, join_requests)
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    ARENA PLUGIN (Spigot)                     │
│  - EventListener (luistert naar Redis berichten)            │
│  - EventModifier (past game mechanics aan)                  │
│  - ArenaJoinHandler (verwerkt rotating joins)               │
└─────────────────────────────────────────────────────────────┘
```

## Stap 1: Redis Communicatie Opzetten

### 1.1 Redis Keys
De proxy plugin slaat het huidige event op in Redis:
- Key: `daily_event_id`
- Value: Event ID (bijv. `METEOR_STORM`, `GRAVITY_SHIFT`, etc.)

### 1.2 Join Event Channel
Maak een Redis PubSub channel voor join requests:
- Channel: `rotating_event_join`
- Message format: `{player_uuid}:{player_name}:{event_id}`

### 1.3 Arena Status Channel  
Bestaand channel voor arena status updates:
- Channel: `arena_status_update`
- Message format: JSON met arena informatie

## Stap 2: Arena Plugin - Event Listener Maken

### 2.1 EventDataManager Class
Maak een class die het huidige event bijhoudt:

```java
package com.yourplugin.bedwars.event;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class EventDataManager {
    private static EventDataManager instance;
    private String currentEventId;
    private JedisPool jedisPool;
    
    public static void init(JedisPool pool) {
        instance = new EventDataManager(pool);
    }
    
    private EventDataManager(JedisPool pool) {
        this.jedisPool = pool;
        loadEventFromRedis();
    }
    
    private void loadEventFromRedis() {
        try (Jedis jedis = jedisPool.getResource()) {
            this.currentEventId = jedis.get("daily_event_id");
            if (this.currentEventId == null) {
                this.currentEventId = "NONE";
            }
        }
    }
    
    public void refreshEvent() {
        loadEventFromRedis();
    }
    
    public String getCurrentEventId() {
        return currentEventId;
    }
    
    public boolean isRotatingEvent() {
        return !currentEventId.equals("NONE");
    }
    
    public static EventDataManager getInstance() {
        return instance;
    }
}
```

### 2.2 Redis Subscriber voor Event Updates
Maak een subscriber die luistert naar event changes:

```java
package com.yourplugin.bedwars.event;

import redis.clients.jedis.JedisPubSub;

public class EventUpdateSubscriber extends JedisPubSub {
    
    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("daily_event_update")) {
            // Event is veranderd, refresh de data
            EventDataManager.getInstance().refreshEvent();
            
            // Log voor debugging
            Bukkit.getLogger().info("[RotatingEvent] Event updated to: " + message);
            
            // Optioneel: broadcast naar alle spelers in rotating arenas
            broadcastEventChange(message);
        }
    }
    
    private void broadcastEventChange(String newEventId) {
        // Zoek alle arenas in de "rotating" group
        for (Arena arena : getArenas()) {
            if (arena.getGroup().equalsIgnoreCase("rotating")) {
                for (Player player : arena.getPlayers()) {
                    player.sendMessage("§6§l[RIFT] §eThe daily event has changed to: §6" + newEventId);
                }
            }
        }
    }
}
```

### 2.3 Start Subscriber in je Main Class
```java
public class YourArenaPlugin extends JavaPlugin {
    private JedisPool jedisPool;
    
    @Override
    public void onEnable() {
        // ... bestaande code ...
        
        // Initialize Event System
        EventDataManager.init(jedisPool);
        
        // Start Redis subscriber in async thread
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                EventUpdateSubscriber subscriber = new EventUpdateSubscriber();
                jedis.subscribe(subscriber, "daily_event_update");
            }
        });
    }
}
```

## Stap 3: Proxy Plugin - Event Update Broadcast

Update `DailyEventManager.java` om updates te broadcasten via Redis:

```java
private void setNewEvent(DailyEvent event) {
    this.currentEvent = event;
    
    // Store in Redis
    if (BedWarsProxy.getRedisConnection() != null) {
        BedWarsProxy.getRedisConnection().storeSetting("daily_event_id", event.getId());
        BedWarsProxy.getRedisConnection().storeSetting("daily_event_date", LocalDate.now().toString());
        
        // NIEUW: Broadcast update via Redis PubSub
        try (Jedis jedis = BedWarsProxy.getRedisConnection().getJedisPool().getResource()) {
            jedis.publish("daily_event_update", event.getId());
        }
    }
    
    // Broadcast to online players
    broadcastEventChange();
}
```

## Stap 4: Rotating Arena Join System

### 4.1 Join Request Handler (Proxy Side)
Update `RotatingEventGUI.java` om join requests via Redis te versturen:

```java
private void handleJoinClick(Player player) {
    // Verstuur join request via Redis
    try (Jedis jedis = BedWarsProxy.getRedisConnection().getJedisPool().getResource()) {
        String message = player.getUniqueId() + ":" + player.getName() + ":" + 
                        DailyEventManager.getInstance().getCurrentEvent().getId();
        jedis.publish("rotating_event_join", message);
    }
    
    player.sendMessage("§6§l[RIFT] §aTeleporting to rotating event arena...");
    player.closeInventory();
}
```

### 4.2 Join Request Handler (Arena Side)
Maak een subscriber die join requests afhandelt:

```java
package com.yourplugin.bedwars.event;

import redis.clients.jedis.JedisPubSub;
import org.bukkit.Bukkit;
import java.util.UUID;

public class RotatingJoinSubscriber extends JedisPubSub {
    
    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("rotating_event_join")) {
            String[] parts = message.split(":");
            UUID playerUuid = UUID.fromString(parts[0]);
            String playerName = parts[1];
            String eventId = parts[2];
            
            // Run on main thread
            Bukkit.getScheduler().runTask(YourPlugin.getInstance(), () -> {
                handleRotatingJoin(playerUuid, playerName, eventId);
            });
        }
    }
    
    private void handleRotatingJoin(UUID uuid, String name, String eventId) {
        // Zoek een beschikbare rotating arena
        Arena targetArena = findAvailableRotatingArena();
        
        if (targetArena == null) {
            // Geen arena beschikbaar, stuur error terug via Redis
            sendJoinError(uuid, "No rotating arenas available");
            return;
        }
        
        // Verify event ID matches
        if (!EventDataManager.getInstance().getCurrentEventId().equals(eventId)) {
            sendJoinError(uuid, "Event has changed, please try again");
            return;
        }
        
        // Join de arena
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            targetArena.addPlayer(player);
            player.sendMessage("§6§l[RIFT] §aWelcome to §6" + eventId + "§a event!");
        } else {
            // Speler is offline/niet op deze server, stuur bungee teleport
            sendBungeeTeleport(name, targetArena.getServerName());
        }
    }
    
    private Arena findAvailableRotatingArena() {
        return getArenas().stream()
            .filter(a -> a.getGroup().equalsIgnoreCase("rotating"))
            .filter(a -> a.getStatus() == ArenaStatus.WAITING || a.getStatus() == ArenaStatus.STARTING)
            .filter(a -> !a.isFull())
            .findFirst()
            .orElse(null);
    }
}
```

## Stap 5: Event Modifiers Implementeren

### 5.1 Event Modifier Interface
```java
package com.yourplugin.bedwars.event.modifiers;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface EventModifier extends Listener {
    String getEventId();
    void onArenaStart();
    void onArenaEnd();
    void onPlayerJoin(Player player);
    void onPlayerDeath(Player player);
}
```

### 5.2 Voorbeeld: Gravity Shift Modifier
```java
package com.yourplugin.bedwars.event.modifiers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GravityShiftModifier implements EventModifier {
    
    @Override
    public String getEventId() {
        return "GRAVITY_SHIFT";
    }
    
    @Override
    public void onArenaStart() {
        // Event wordt gestart
    }
    
    @Override
    public void onArenaEnd() {
        // Event wordt beëindigd
    }
    
    @Override
    public void onPlayerJoin(Player player) {
        // Geef Jump Boost III
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.JUMP, 
            Integer.MAX_VALUE, 
            2, 
            false, 
            false
        ));
        player.sendMessage("§6§l[GRAVITY SHIFT] §eYou feel lighter...");
    }
    
    @Override
    public void onPlayerDeath(Player player) {
        // Remove effects bij death
        player.removePotionEffect(PotionEffectType.JUMP);
    }
}
```

### 5.3 Event Modifier Manager
```java
package com.yourplugin.bedwars.event;

import com.yourplugin.bedwars.event.modifiers.*;
import java.util.HashMap;
import java.util.Map;

public class EventModifierManager {
    private static EventModifierManager instance;
    private Map<String, EventModifier> modifiers = new HashMap<>();
    
    public static void init() {
        instance = new EventModifierManager();
    }
    
    private EventModifierManager() {
        registerModifiers();
    }
    
    private void registerModifiers() {
        register(new GravityShiftModifier());
        register(new MeteorStormModifier());
        register(new CosmicChaosModifier());
        register(new SolarFlareModifier());
        register(new NebulaNightModifier());
        register(new AsteroidBeltModifier());
        register(new WarpSpeedModifier());
    }
    
    private void register(EventModifier modifier) {
        modifiers.put(modifier.getEventId(), modifier);
        Bukkit.getPluginManager().registerEvents(modifier, YourPlugin.getInstance());
    }
    
    public EventModifier getCurrentModifier() {
        String eventId = EventDataManager.getInstance().getCurrentEventId();
        return modifiers.get(eventId);
    }
    
    public boolean hasActiveModifier() {
        return getCurrentModifier() != null;
    }
    
    public static EventModifierManager getInstance() {
        return instance;
    }
}
```

### 5.4 Integratie in Arena Class
```java
public class Arena {
    
    public void start() {
        // ... bestaande start code ...
        
        // Check if this is a rotating arena
        if (this.getGroup().equalsIgnoreCase("rotating")) {
            EventModifier modifier = EventModifierManager.getInstance().getCurrentModifier();
            if (modifier != null) {
                modifier.onArenaStart();
            }
        }
    }
    
    public void addPlayer(Player player) {
        // ... bestaande join code ...
        
        // Apply event modifiers
        if (this.getGroup().equalsIgnoreCase("rotating")) {
            EventModifier modifier = EventModifierManager.getInstance().getCurrentModifier();
            if (modifier != null) {
                modifier.onPlayerJoin(player);
            }
        }
    }
}
```

## Stap 6: Arena Herkenning als Rotating Event

### 6.1 Arena Config
Voeg een flag toe aan je arena configuratie:

```yaml
# arenas/rift_arena_1.yml
name: "Rift Arena 1"
world: "rift_world_1"
group: "rotating"  # Dit maakt het een rotating event arena
min-players: 2
max-players: 8
# ... rest van config
```

### 6.2 Auto-detect Rotating Arenas
```java
public boolean isRotatingArena() {
    return this.getGroup() != null && this.getGroup().equalsIgnoreCase("rotating");
}
```

## Stap 7: PlaceholderAPI Integration

De proxy plugin heeft nu de volgende placeholders:

### Beschikbare Placeholders:
- `%bw2023_daily_event_name%` - Naam van current event (bijv. "Meteorite Storm")
- `%bw2023_daily_event_id%` - ID van current event (bijv. "METEOR_STORM")
- `%bw2023_daily_event_description%` - Eerste regel van beschrijving

### Gebruik in Arena Plugin:
```java
import me.clip.placeholderapi.PlaceholderAPI;

String message = PlaceholderAPI.setPlaceholders(player, 
    "&6Today's Event: &e%bw2023_daily_event_name%");
player.sendMessage(message);
```

### Scoreboard Integratie:
```java
// In je scoreboard class
scoreboard.setLine(5, PlaceholderAPI.setPlaceholders(player, 
    "&6Event: &e%bw2023_daily_event_name%"));
```

## Stap 8: Testing Checklist

### Proxy Side:
- [ ] DailyEventManager initialize correct
- [ ] Event wordt opgeslagen in Redis
- [ ] Event update wordt gebroadcast via Redis PubSub
- [ ] GUI toont correct event
- [ ] Join button stuurt Redis message
- [ ] PlaceholderAPI werkt

### Arena Side:
- [ ] EventDataManager laadt event ID uit Redis
- [ ] Redis subscriber ontvangt event updates
- [ ] Join requests worden ontvangen
- [ ] Speler wordt correct naar rotating arena gestuurd
- [ ] Event modifiers worden toegepast
- [ ] Rotating arenas worden herkend
- [ ] Event effects worden verwijderd bij death/leave

### End-to-End:
- [ ] Speler klikt join in GUI → komt aan in rotating arena
- [ ] Event modifiers worden toegepast bij join
- [ ] Event verandert om 00:00 → alle servers worden geüpdatet
- [ ] Nieuwe spelers krijgen nieuwe event modifiers
- [ ] Oude arenas blijven oude event afmaken (optioneel)

## Stap 9: Event Voorbeelden - Volledige Implementatie

### Meteorite Storm
```java
public class MeteorStormModifier implements EventModifier {
    private Map<Arena, BukkitTask> meteorTasks = new HashMap<>();
    
    @Override
    public void onArenaStart() {
        // Start meteorite spawning
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // 2x meteorite shard drops voor bepaalde blocks
        if (e.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            // Drop extra items
        }
    }
}
```

### Solar Flare
```java
public class SolarFlareModifier implements EventModifier {
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Block above = player.getLocation().add(0, 1, 0).getBlock();
        
        // Check if player is under open sky
        if (above.getLightFromSky() == 15) {
            // Apply fire damage
            player.setFireTicks(40);
        }
    }
}
```

## Stap 10: Advanced Features

### 10.1 Event History
```java
// Track welke events deze week al geweest zijn
public class EventHistoryManager {
    public void trackEvent(String eventId) {
        try (Jedis jedis = pool.getResource()) {
            String weekKey = "event_history:" + getWeekNumber();
            jedis.sadd(weekKey, eventId);
            jedis.expire(weekKey, 604800); // 7 dagen
        }
    }
}
```

### 10.2 Event Specific Stats
```java
// Track stats per event type
public class EventStatsManager {
    public void incrementEventWin(UUID player, String eventId) {
        // event_wins:METEOR_STORM:uuid
        String key = "event_wins:" + eventId + ":" + player;
        // ... increment in database
    }
}
```

### 10.3 Cross-Server Synchronization
```java
// Zorg dat alle arena servers hetzelfde event hebben
public class EventSyncManager {
    public void syncCheck() {
        String proxyEvent = getEventFromRedis();
        String localEvent = EventDataManager.getInstance().getCurrentEventId();
        
        if (!proxyEvent.equals(localEvent)) {
            EventDataManager.getInstance().refreshEvent();
            reloadActiveArenas();
        }
    }
}
```

## Troubleshooting

### Event wordt niet geüpdatet in arena plugin
1. Check Redis connectie
2. Verify subscriber is actief
3. Check logs voor exceptions
4. Test handmatig: `PUBLISH daily_event_update METEOR_STORM` in Redis CLI

### Join werkt niet
1. Check of rotating_event_join channel actief is
2. Verify arena group = "rotating" in config
3. Check of er beschikbare arenas zijn
4. Check player permissions

### Event modifiers werken niet
1. Verify EventModifierManager is geïnitialiseerd
2. Check of event ID matcht (case sensitive!)
3. Verify Listener is registered
4. Check console voor errors

## Conclusie

Met dit systeem:
- ✅ Proxy beheert welk event actief is
- ✅ Redis synchroniseert tussen alle servers
- ✅ Arena plugin past game mechanics aan
- ✅ Spelers kunnen joinen via GUI
- ✅ Events roteren automatisch dagelijks
- ✅ Alles blijft gesynchroniseerd

Je hoeft geen Hook te maken, Redis PubSub IS je communicatie systeem!

