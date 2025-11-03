# Implementation Checklist - Arena Event Integration

## Quick Start Guide

### Fase 1: Proxy Plugin Updates ✅ (Al gedaan)
- [x] DailyEventManager gemaakt
- [x] RotatingEventGUI gemaakt  
- [x] Redis storage voor events
- [x] Daily rotation systeem
- [ ] **TODO: Event broadcasting via Redis** (zie ARENA_INTEGRATION.md stap 1.2)

### Fase 2: Arena Plugin Basis (Start hier!)
- [ ] Maak `DailyEventManager.java` in arena plugin
- [ ] Maak `EventHandler.java` interface
- [ ] Maak `EventHandlerFactory.java`
- [ ] Initialize DailyEventManager in main class
- [ ] Test dat event ID wordt geladen uit Redis

### Fase 3: Eerste Event Handler (Warp Speed - Makkelijkste)
- [ ] Maak `WarpSpeedHandler.java`
- [ ] Implement Speed III + Haste II logic
- [ ] Test in development omgeving
- [ ] Verify dat effects worden toegepast bij join
- [ ] Verify dat effects worden verwijderd bij leave

### Fase 4: Arena Lifecycle Hooks
- [ ] Hook event system in arena start event
- [ ] Hook event system in arena end event
- [ ] Hook event system in player join event
- [ ] Hook event system in player leave event
- [ ] Test complete flow: join → play → leave

### Fase 5: Redis Real-time Sync
- [ ] Maak RedisEventListener voor live updates
- [ ] Broadcast events vanuit proxy (stap 1.2)
- [ ] Test event rotation zonder restart
- [ ] Verify dat alle game servers syncen

### Fase 6: Overige Event Handlers
- [ ] MeteorStormHandler (meteorite spawns, explosies)
- [ ] GravityShiftHandler (jump boost, slow falling)
- [ ] CosmicChaosHandler (random events elke 5 min)
- [ ] SolarFlareHandler (damage bij sunlight)
- [ ] NebulaNightHandler (night time, glowing ores)
- [ ] AsteroidBeltHandler (floating platforms)

### Fase 7: Testing & Polish
- [ ] Test alle 7 events
- [ ] Test event rotation bij midnight
- [ ] Test met meerdere arenas tegelijk
- [ ] Test persistence over restarts
- [ ] Fix bugs en performance issues
- [ ] Voeg admin commands toe (force event, etc.)

### Fase 8: Production Deployment
- [ ] Backup huidige plugins
- [ ] Deploy proxy plugin update
- [ ] Deploy arena plugin update
- [ ] Monitor logs voor errors
- [ ] Test met echte spelers

---

## Belangrijkste Code Snippets

### 1. Proxy Plugin - Event Broadcasting (TOEVOEGEN)
```java
// In DailyEventManager.java na rotateToNewEvent()
private void broadcastEventToGameServers() {
    JsonObject message = new JsonObject();
    message.addProperty("type", "EVENT_SYNC");
    message.addProperty("event_id", currentEvent.getId());
    message.addProperty("event_name", currentEvent.getName());
    BedWarsProxy.getRedisConnection().sendMessage(message, "bedwars-events");
}
```

### 2. Arena Plugin - Event Manager Init
```java
// In BedWars.java onEnable()
Bukkit.getScheduler().runTaskLater(this, () -> {
    DailyEventManager.getInstance().init();
}, 100L);
```

### 3. Arena Plugin - Arena Start Hook
```java
// In game start listener
if (arena.getGroup().equals("Rotating")) {
    DailyEventManager.getInstance().applyEventToArena(arena);
}
```

---

## Redis Keys Reference

| Key | Type | Example Value | Beschrijving |
|-----|------|---------------|--------------|
| `daily_event_id` | String | `"WARP_SPEED"` | Huidige event ID |
| `daily_event_date` | String | `"2025-11-03"` | Datum van huidige event |

---

## Testing Commands

```bash
# Check Redis keys (in Redis CLI)
GET daily_event_id
GET daily_event_date

# Admin commands (in-game)
/bw gui rotating          # Open event GUI
/bw dailyevent info       # Show current event
/bw dailyevent list       # List all events

# Force event rotation (voor testing)
# Voeg deze command toe in proxy plugin:
/bw dailyevent rotate     # Force new random event
/bw dailyevent set <id>   # Set specific event
```

---

## Development Tips

### Start Klein
Begin met **alleen Warp Speed** event. Dit is het makkelijkste omdat het alleen potion effects zijn. Als dit werkt, weet je dat je basis infrastructuur klopt.

### Debug Logging
Voeg extensive logging toe tijdens development:
```java
BedWars.plugin.getLogger().info("[DEBUG] Loading event: " + eventId);
BedWars.plugin.getLogger().info("[DEBUG] Applied to arena: " + arena.getName());
```

### Test in Lokale Omgeving
Test eerst met 1 proxy + 1 game server lokaal voordat je deploy naar productie.

### Code Structure
```
arena-plugin/
└── src/main/java/com/andrei1058/bedwars/
    └── events/
        ├── DailyEventManager.java       (Hoofd manager)
        ├── EventHandler.java             (Interface)
        ├── EventHandlerFactory.java      (Factory pattern)
        └── handlers/
            ├── WarpSpeedHandler.java
            ├── MeteorStormHandler.java
            ├── GravityShiftHandler.java
            ├── CosmicChaosHandler.java
            ├── SolarFlareHandler.java
            ├── NebulaNightHandler.java
            └── AsteroidBeltHandler.java
```

---

## Veelvoorkomende Problemen

### "Event not loading from Redis"
→ Check of Redis connection actief is  
→ Verify dat keys bestaan met `GET daily_event_id` in Redis CLI  
→ Check of `retrieveSetting()` methode bestaat in je arena plugin

### "Effects not applying to players"
→ Verify dat arena group = "Rotating"  
→ Check of EventHandler.enable() wordt aangeroepen  
→ Test met debug messages in applyToArena()

### "Event not persisting across restart"
→ Check Redis persistence config (RDB/AOF)  
→ Verify dat proxy schrijft naar Redis bij rotation  
→ Test met `redis-cli GET daily_event_id`

### "Multiple servers out of sync"
→ Implement Redis broadcast (stap 1.2)  
→ Check dat alle servers dezelfde Redis gebruiken  
→ Verify dat pub/sub listener actief is

---

## Performance Overwegingen

### Potion Effects
Gebruik `amplifier` en `duration` efficient:
```java
// ❌ BAD: Elke tick renew = lag
player.addPotionEffect(new PotionEffect(type, 1, 2));

// ✅ GOOD: Langere duration, minder frequent
player.addPotionEffect(new PotionEffect(type, 100, 2));
```

### Scheduled Tasks
```java
// ❌ BAD: Te frequente checks
Bukkit.getScheduler().runTaskTimer(plugin, task, 0L, 1L); // Elke tick!

// ✅ GOOD: Redelijke intervals
Bukkit.getScheduler().runTaskTimer(plugin, task, 0L, 80L); // Elke 4 sec
```

### Redis Calls
```java
// ❌ BAD: Sync Redis call in main thread
String value = redis.get("key"); // Kan lag veroorzaken

// ✅ GOOD: Async Redis calls
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    String value = redis.get("key");
});
```

---

## Volgende Stappen Na Implementatie

1. **Event Statistics Tracking** - Sla op hoeveel games per event gespeeld zijn
2. **Event Rotation Schedule** - Zorg dat elk event 1x per week voorkomt
3. **Custom Event Config** - Laat admins event parameters tweaken
4. **Event Rewards** - Extra beloningen voor specifieke events
5. **Event Achievements** - "Win 10 games during Meteor Storm"
6. **API Events** - Fire custom events voor andere plugins:
   ```java
   Bukkit.getPluginManager().callEvent(new DailyEventChangeEvent(oldEvent, newEvent));
   ```

---

## Resources

- **Volledige Guide**: `ARENA_INTEGRATION.md`
- **Event Systeem Info**: `DAILY_EVENT_SYSTEM.md`
- **BedWars API Docs**: Check je bestaande API documentation
- **Redis Commands**: https://redis.io/commands

---

## Contact & Support

Als je vast loopt:
1. Check console logs voor errors
2. Test met debug logging
3. Verify Redis connectivity
4. Check dat beide plugins up-to-date zijn

Good luck met de implementatie! 🚀

