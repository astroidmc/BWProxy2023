# Quick Implementation Guide - File Structure

## Proxy Plugin Updates (Deze Repo) ✅

Deze zijn al geïmplementeerd, maar je moet nog 1 ding toevoegen:

### ✅ Al Klaar
- `proxy-plugin/src/main/java/com/tomkeuper/bedwars/proxy/arenamanager/DailyEventManager.java`
- `proxy-plugin/src/main/java/com/tomkeuper/bedwars/proxy/command/main/DailyEventCMD.java`
- `proxy-plugin/src/main/java/com/tomkeuper/bedwars/proxy/gui/RotatingEventGUI.java`

### 🔧 Deployment
1. Build: `mvn clean package`
2. Upload: `proxy-plugin/target/proxy-plugin-1.0.jar`
3. Restart proxy server

---

## Arena Plugin Files (Nieuw te Maken)

### Core System (Verplicht)

```
arena-plugin/src/main/java/com/andrei1058/bedwars/
└── events/
    ├── DailyEventManager.java          ← START HIER
    ├── EventHandler.java               ← Interface
    ├── EventHandlerFactory.java        ← Factory pattern
    └── handlers/                       ← Event implementations
        ├── WarpSpeedHandler.java       ← START HIER (makkelijkste)
        ├── GravityShiftHandler.java
        ├── NebulaNightHandler.java
        ├── MeteorStormHandler.java
        ├── CosmicChaosHandler.java
        ├── SolarFlareHandler.java
        └── AsteroidBeltHandler.java
```

### Listeners (Optioneel maar Aangeraden)

```
arena-plugin/src/main/java/com/andrei1058/bedwars/
└── listeners/
    └── RedisEventListener.java         ← Voor real-time sync
```

---

## Implementation Volgorde

### Stap 1: Core Files (30 min)
Maak deze 3 bestanden eerst:

1. **EventHandler.java** (interface)
   - Copy van `EVENT_HANDLER_EXAMPLES.md` sectie 1.2
   - Plaats in: `events/EventHandler.java`

2. **DailyEventManager.java** 
   - Copy van `ARENA_INTEGRATION.md` sectie 2.1
   - Plaats in: `events/DailyEventManager.java`

3. **EventHandlerFactory.java**
   - Copy van `ARENA_INTEGRATION.md` sectie 2.3
   - Plaats in: `events/EventHandlerFactory.java`

### Stap 2: Initialize System (10 min)

In je **BedWars.java** (main class):

```java
@Override
public void onEnable() {
    // ... bestaande code ...
    
    // Initialize daily event system
    Bukkit.getScheduler().runTaskLater(this, () -> {
        DailyEventManager.getInstance().init();
    }, 100L);
}
```

### Stap 3: Eerste Event Handler (20 min)

1. Maak folder: `events/handlers/`

2. **WarpSpeedHandler.java**
   - Copy COMPLETE code van `EVENT_HANDLER_EXAMPLES.md`
   - Test eerst alleen deze!

### Stap 4: Hook in Arena Lifecycle (20 min)

In je arena event listener (waar game start/end wordt afgehandeld):

```java
// Bij game start
@EventHandler
public void onGameStart(GameStartEvent e) {
    IArena arena = e.getArena();
    if (arena.getGroup().equals("Rotating")) {
        DailyEventManager.getInstance().applyEventToArena(arena);
    }
}

// Bij player join
@EventHandler  
public void onPlayerJoinArena(PlayerJoinArenaEvent e) {
    if (e.getArena().getGroup().equals("Rotating")) {
        EventHandler handler = DailyEventManager.getInstance().getCurrentEventHandler();
        if (handler != null) {
            handler.onPlayerJoin(e.getPlayer(), e.getArena());
        }
    }
}

// Bij player leave
@EventHandler
public void onPlayerLeaveArena(PlayerLeaveArenaEvent e) {
    if (e.getArena().getGroup().equals("Rotating")) {
        EventHandler handler = DailyEventManager.getInstance().getCurrentEventHandler();
        if (handler != null) {
            handler.onPlayerLeave(e.getPlayer(), e.getArena());
        }
    }
}
```

### Stap 5: Test! (15 min)

```
1. Compile arena plugin
2. Upload naar test server
3. Start server
4. Check logs: "Loaded daily event from Redis: WARP_SPEED"
5. Join rotating arena
6. Verify Speed III + Haste II
```

### Stap 6: Overige Handlers (1-2 uur)

Als Warp Speed werkt, voeg toe (in volgorde van moeilijkheid):

1. ✅ **GravityShiftHandler** - Ook alleen potion effects
2. ✅ **NebulaNightHandler** - World time manipulation
3. ⚠️ **MeteorStormHandler** - Entity spawning (complex)
4. ⚠️ **CosmicChaosHandler** - Random events (complex)
5. ⚠️ **SolarFlareHandler** - Environmental damage (complex)
6. ⚠️ **AsteroidBeltHandler** - Structure spawning (very complex)

---

## Code Templates

### EventHandler.java
```java
package com.andrei1058.bedwars.events;

import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;

public interface EventHandler {
    String getEventId();
    String getEventName();
    void enable();
    void disable();
    void applyToArena(IArena arena);
    void removeFromArena(IArena arena);
    void onPlayerJoin(Player player, IArena arena);
    void onPlayerLeave(Player player, IArena arena);
}
```

### Minimal Handler Template
```java
package com.andrei1058.bedwars.events.handlers;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.events.EventHandler;
import org.bukkit.entity.Player;

public class TemplateHandler implements EventHandler {
    
    @Override
    public String getEventId() {
        return "EVENT_ID";
    }
    
    @Override
    public String getEventName() {
        return "Event Name";
    }
    
    @Override
    public void enable() {
        // Start tasks, register listeners
    }
    
    @Override
    public void disable() {
        // Cancel tasks, unregister listeners
    }
    
    @Override
    public void applyToArena(IArena arena) {
        // Apply event to arena when game starts
    }
    
    @Override
    public void removeFromArena(IArena arena) {
        // Remove event from arena when game ends
    }
    
    @Override
    public void onPlayerJoin(Player player, IArena arena) {
        // Send message, apply effects
    }
    
    @Override
    public void onPlayerLeave(Player player, IArena arena) {
        // Remove effects
    }
    
    private boolean isRotatingArena(IArena arena) {
        return arena.getGroup().equalsIgnoreCase("Rotating");
    }
}
```

---

## Testing Checklist

### Basis Functionaliteit
- [ ] Server start zonder errors
- [ ] DailyEventManager initializes
- [ ] Event wordt geladen uit Redis
- [ ] Correct event ID in logs
- [ ] EventHandlerFactory returnt correct handler

### Warp Speed Event
- [ ] Speed III wordt toegepast bij join
- [ ] Haste II wordt toegepast bij join  
- [ ] Effects blijven tijdens game
- [ ] Effects worden verwijderd bij leave
- [ ] Join message wordt getoond

### Arena Integration
- [ ] Event wordt toegepast bij game start
- [ ] Alle spelers krijgen effects
- [ ] Event wordt verwijderd bij game end
- [ ] Werkt met meerdere arenas tegelijk

### Persistence
- [ ] Event blijft hetzelfde na server restart
- [ ] Redis keys zijn correct
- [ ] Event rotatie werkt om middernacht

---

## Common Issues & Fixes

### "Event is null"
**Probleem:** `getCurrentEventHandler()` returnt null

**Fix:** 
1. Check of event ID bestaat in Redis: `GET daily_event_id`
2. Verify EventHandlerFactory heeft de case voor deze ID
3. Check logs voor initialization errors

### "Effects not applying"
**Probleem:** Spelers krijgen geen potion effects

**Fix:**
1. Verify `enable()` wordt aangeroepen
2. Check of arena group matcht (gebruik `.equalsIgnoreCase()`)
3. Add debug logging in `applyToArena()`

### "Task not running"
**Probleem:** Scheduled task start niet

**Fix:**
```java
// Verify plugin instance
Bukkit.getScheduler().runTaskTimer(
    BedWars.plugin,  // ← Check dit is correct
    task, 
    delay, 
    period
);
```

### "Redis connection failed"
**Probleem:** Kan event niet laden uit Redis

**Fix:**
1. Check Redis is running: `redis-cli PING`
2. Verify connection settings in config
3. Test met: `BedWars.getRemoteDatabase().getString("daily_event_id")`

---

## Deployment Checklist

### Pre-Deployment
- [ ] Backup huidige arena plugin
- [ ] Test in development environment
- [ ] All handlers werkend
- [ ] No console errors
- [ ] Performance is acceptable

### Deployment
- [ ] Stop alle game servers
- [ ] Upload nieuwe arena plugin jar
- [ ] Start servers in sequence
- [ ] Monitor logs voor errors
- [ ] Test met 1 player eerst

### Post-Deployment
- [ ] Verify event is active
- [ ] Test player join flow
- [ ] Check Redis keys
- [ ] Monitor server performance
- [ ] Get player feedback

---

## Estimated Time

| Task | Time |
|------|------|
| Core files setup | 30 min |
| Warp Speed handler | 20 min |
| Arena hooks | 20 min |
| Testing & debugging | 30 min |
| Additional handlers (x6) | 2-3 hours |
| **Total** | **~4 hours** |

---

## Quick Commands Reference

### Maven Build
```bash
# Proxy plugin
cd proxy-plugin
mvn clean package

# Arena plugin  
cd arena-plugin
mvn clean package
```

### Redis Check
```bash
redis-cli
> GET daily_event_id
> GET daily_event_date
> KEYS daily_*
```

### In-Game Testing
```
/bw gui rotating          - Open GUI
/bw dailyevent info       - Current event
/bw join Rotating         - Join rotating arena
```

---

## Resources

📁 **Documentation Files:**
- `ARENA_INTEGRATION.md` - Complete implementation guide
- `IMPLEMENTATION_CHECKLIST.md` - Step-by-step checklist
- `EVENT_HANDLER_EXAMPLES.md` - Code examples
- `DAILY_EVENT_SYSTEM.md` - Proxy system overview
- `QUICK_START.md` - This file!

🔧 **Code Templates:**
- All event handlers have complete examples
- Copy-paste ready code snippets
- Tested and working implementations

---

**Start with Warp Speed, get it working, then expand! 🚀**

