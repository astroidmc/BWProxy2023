# Example Event Handler Implementation

Dit bestand bevat voorbeelden van event handlers die je direct kunt gebruiken in de arena plugin.

---

## 1. Warp Speed Handler (Complete Example)

**Bestand:** `handlers/WarpSpeedHandler.java`

```java
package com.andrei1058.bedwars.events.handlers;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.events.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class WarpSpeedHandler implements EventHandler {
    
    private BukkitTask speedTask;
    
    @Override
    public String getEventId() {
        return "WARP_SPEED";
    }
    
    @Override
    public String getEventName() {
        return "Warp Speed";
    }
    
    @Override
    public void enable() {
        // Start task that gives speed and haste to all players in Rotating arenas
        speedTask = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            for (IArena arena : BedWars.getArenaUtil().getArenas()) {
                if (isRotatingArena(arena) && arena.isPlaying()) {
                    for (Player player : arena.getPlayers()) {
                        // Speed III
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED, 100, 2, false, false
                        ));
                        // Haste II
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.FAST_DIGGING, 100, 1, false, false
                        ));
                    }
                }
            }
        }, 0L, 80L); // Every 4 seconds
        
        BedWars.plugin.getLogger().info("[Warp Speed] Event handler enabled!");
    }
    
    @Override
    public void disable() {
        if (speedTask != null) {
            speedTask.cancel();
        }
        
        // Remove effects from all players
        for (IArena arena : BedWars.getArenaUtil().getArenas()) {
            if (isRotatingArena(arena)) {
                for (Player player : arena.getPlayers()) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                }
            }
        }
        
        BedWars.plugin.getLogger().info("[Warp Speed] Event handler disabled!");
    }
    
    @Override
    public void applyToArena(IArena arena) {
        arena.sendMessage("&6&lWarp Speed &7event is active! Everyone has Speed III and Haste II!");
    }
    
    @Override
    public void removeFromArena(IArena arena) {
        for (Player player : arena.getPlayers()) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        }
    }
    
    @Override
    public void onPlayerJoin(Player player, IArena arena) {
        player.sendMessage("§8§m                                        ");
        player.sendMessage("§b§lThe Rift §8» §6§lWarp Speed Event");
        player.sendMessage("§7Everything moves faster!");
        player.sendMessage("§7• Permanent Speed III");
        player.sendMessage("§7• Haste II effect");
        player.sendMessage("§8§m                                        ");
    }
    
    @Override
    public void onPlayerLeave(Player player, IArena arena) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
    }
    
    private boolean isRotatingArena(IArena arena) {
        return arena.getGroup().equalsIgnoreCase("Rotating") || 
               arena.getArenaName().toLowerCase().contains("rotating");
    }
}
```

---

## 2. Gravity Shift Handler

```java
package com.andrei1058.bedwars.events.handlers;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.events.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class GravityShiftHandler implements EventHandler {
    
    private BukkitTask gravityTask;
    
    @Override
    public String getEventId() {
        return "GRAVITY_SHIFT";
    }
    
    @Override
    public String getEventName() {
        return "Gravity Shift";
    }
    
    @Override
    public void enable() {
        gravityTask = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            for (IArena arena : BedWars.getArenaUtil().getArenas()) {
                if (isRotatingArena(arena) && arena.isPlaying()) {
                    for (Player player : arena.getPlayers()) {
                        // Jump Boost III
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.JUMP, 100, 2, false, false
                        ));
                        // Slow Falling
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOW_FALLING, 100, 0, false, false
                        ));
                    }
                }
            }
        }, 0L, 80L);
        
        BedWars.plugin.getLogger().info("[Gravity Shift] Event handler enabled!");
    }
    
    @Override
    public void disable() {
        if (gravityTask != null) {
            gravityTask.cancel();
        }
        
        for (IArena arena : BedWars.getArenaUtil().getArenas()) {
            if (isRotatingArena(arena)) {
                for (Player player : arena.getPlayers()) {
                    player.removePotionEffect(PotionEffectType.JUMP);
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                }
            }
        }
        
        BedWars.plugin.getLogger().info("[Gravity Shift] Event handler disabled!");
    }
    
    @Override
    public void applyToArena(IArena arena) {
        arena.sendMessage("&b&lGravity Shift &7event is active! Jump higher, fall slower!");
    }
    
    @Override
    public void removeFromArena(IArena arena) {
        for (Player player : arena.getPlayers()) {
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
    }
    
    @Override
    public void onPlayerJoin(Player player, IArena arena) {
        player.sendMessage("§8§m                                        ");
        player.sendMessage("§b§lThe Rift §8» §b§lGravity Shift Event");
        player.sendMessage("§7Experience altered gravity!");
        player.sendMessage("§7• Enhanced jump boost III");
        player.sendMessage("§7• Slow falling effect");
        player.sendMessage("§8§m                                        ");
    }
    
    @Override
    public void onPlayerLeave(Player player, IArena arena) {
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
    }
    
    private boolean isRotatingArena(IArena arena) {
        return arena.getGroup().equalsIgnoreCase("Rotating");
    }
}
```

---

## 3. Nebula Night Handler

```java
package com.andrei1058.bedwars.events.handlers;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.events.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class NebulaNightHandler implements EventHandler {
    
    private BukkitTask nightTask;
    private final Set<String> affectedWorlds = new HashSet<>();
    
    @Override
    public String getEventId() {
        return "NEBULA_NIGHT";
    }
    
    @Override
    public String getEventName() {
        return "Nebula Night";
    }
    
    @Override
    public void enable() {
        nightTask = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            for (IArena arena : BedWars.getArenaUtil().getArenas()) {
                if (isRotatingArena(arena) && arena.isPlaying()) {
                    // Set world time to night
                    World world = arena.getWorld();
                    if (world != null) {
                        world.setTime(18000); // Midnight
                        affectedWorlds.add(world.getName());
                    }
                    
                    // Give night vision to players
                    for (Player player : arena.getPlayers()) {
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.NIGHT_VISION, 300, 0, false, false
                        ));
                    }
                }
            }
        }, 0L, 100L); // Every 5 seconds
        
        BedWars.plugin.getLogger().info("[Nebula Night] Event handler enabled!");
    }
    
    @Override
    public void disable() {
        if (nightTask != null) {
            nightTask.cancel();
        }
        
        // Reset world times
        for (String worldName : affectedWorlds) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                world.setTime(6000); // Day
            }
        }
        affectedWorlds.clear();
        
        // Remove night vision
        for (IArena arena : BedWars.getArenaUtil().getArenas()) {
            if (isRotatingArena(arena)) {
                for (Player player : arena.getPlayers()) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }
        
        BedWars.plugin.getLogger().info("[Nebula Night] Event handler disabled!");
    }
    
    @Override
    public void applyToArena(IArena arena) {
        arena.sendMessage("&5&lNebula Night &7event is active! Darkness falls across The Rift!");
        
        World world = arena.getWorld();
        if (world != null) {
            world.setTime(18000);
            affectedWorlds.add(world.getName());
        }
    }
    
    @Override
    public void removeFromArena(IArena arena) {
        for (Player player : arena.getPlayers()) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        
        World world = arena.getWorld();
        if (world != null) {
            world.setTime(6000);
            affectedWorlds.remove(world.getName());
        }
    }
    
    @Override
    public void onPlayerJoin(Player player, IArena arena) {
        player.sendMessage("§8§m                                        ");
        player.sendMessage("§b§lThe Rift §8» §5§lNebula Night Event");
        player.sendMessage("§7Darkness falls across The Rift!");
        player.sendMessage("§7• Permanent night time");
        player.sendMessage("§7• Night vision provided");
        player.sendMessage("§8§m                                        ");
        
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.NIGHT_VISION, 999999, 0, false, false
        ));
    }
    
    @Override
    public void onPlayerLeave(Player player, IArena arena) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
    
    private boolean isRotatingArena(IArena arena) {
        return arena.getGroup().equalsIgnoreCase("Rotating");
    }
}
```

---

## 4. Meteor Storm Handler (Advanced)

```java
package com.andrei1058.bedwars.events.handlers;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.events.EventHandler;
import org.bukkit.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class MeteorStormHandler implements EventHandler {
    
    private BukkitTask meteorTask;
    private final Random random = new Random();
    
    @Override
    public String getEventId() {
        return "METEOR_STORM";
    }
    
    @Override
    public String getEventName() {
        return "Meteorite Storm";
    }
    
    @Override
    public void enable() {
        // Spawn meteorites periodically
        meteorTask = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            for (IArena arena : BedWars.getArenaUtil().getArenas()) {
                if (isRotatingArena(arena) && arena.isPlaying() && arena.getPlayers().size() > 0) {
                    spawnMeteor(arena);
                }
            }
        }, 100L, 200L); // Every 10 seconds
        
        BedWars.plugin.getLogger().info("[Meteor Storm] Event handler enabled!");
    }
    
    @Override
    public void disable() {
        if (meteorTask != null) {
            meteorTask.cancel();
        }
        
        BedWars.plugin.getLogger().info("[Meteor Storm] Event handler disabled!");
    }
    
    @Override
    public void applyToArena(IArena arena) {
        arena.sendMessage("&c&lMeteor Storm &7event is active! Watch the skies!");
    }
    
    @Override
    public void removeFromArena(IArena arena) {
        // No cleanup needed
    }
    
    @Override
    public void onPlayerJoin(Player player, IArena arena) {
        player.sendMessage("§8§m                                        ");
        player.sendMessage("§b§lThe Rift §8» §c§lMeteor Storm Event");
        player.sendMessage("§7Meteorites rain from the sky!");
        player.sendMessage("§7• Random meteorite impacts");
        player.sendMessage("§7• Bonus resources from space rocks");
        player.sendMessage("§8§m                                        ");
    }
    
    @Override
    public void onPlayerLeave(Player player, IArena arena) {
        // No cleanup needed
    }
    
    private void spawnMeteor(IArena arena) {
        World world = arena.getWorld();
        if (world == null) return;
        
        // Get random player location as target area
        if (arena.getPlayers().isEmpty()) return;
        Player randomPlayer = (Player) arena.getPlayers().toArray()[random.nextInt(arena.getPlayers().size())];
        Location targetLoc = randomPlayer.getLocation();
        
        // Randomize location a bit
        double offsetX = (random.nextDouble() - 0.5) * 30; // ±15 blocks
        double offsetZ = (random.nextDouble() - 0.5) * 30;
        
        Location spawnLoc = new Location(
            world,
            targetLoc.getX() + offsetX,
            world.getMaxHeight() - 10,
            targetLoc.getZ() + offsetZ
        );
        
        // Spawn falling block
        Material meteorMaterial = random.nextBoolean() ? Material.MAGMA_BLOCK : Material.NETHERRACK;
        FallingBlock meteor = world.spawnFallingBlock(spawnLoc, meteorMaterial.createBlockData());
        meteor.setDropItem(false);
        meteor.setHurtEntities(true);
        
        // Visual and sound effects
        world.playSound(spawnLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
        world.spawnParticle(Particle.FLAME, spawnLoc, 50, 0.5, 0.5, 0.5, 0.1);
        
        // Schedule impact effect
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
            Location impactLoc = meteor.getLocation();
            if (!meteor.isDead()) {
                meteor.remove();
            }
            
            // Impact effects
            world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            world.spawnParticle(Particle.EXPLOSION_LARGE, impactLoc, 3);
            world.spawnParticle(Particle.LAVA, impactLoc, 20, 2, 0.5, 2);
            
            // Small explosion (no block damage)
            world.createExplosion(impactLoc, 2.0f, false, false);
            
        }, 80L); // 4 seconds fall time
    }
    
    private boolean isRotatingArena(IArena arena) {
        return arena.getGroup().equalsIgnoreCase("Rotating");
    }
}
```

---

## 5. Cosmic Chaos Handler (Random Events)

```java
package com.andrei1058.bedwars.events.handlers;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.events.EventHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class CosmicChaosHandler implements EventHandler {
    
    private BukkitTask chaosTask;
    private final Random random = new Random();
    
    @Override
    public String getEventId() {
        return "COSMIC_CHAOS";
    }
    
    @Override
    public String getEventName() {
        return "Cosmic Chaos";
    }
    
    @Override
    public void enable() {
        // Trigger random events every 5 minutes
        chaosTask = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            for (IArena arena : BedWars.getArenaUtil().getArenas()) {
                if (isRotatingArena(arena) && arena.isPlaying()) {
                    triggerRandomEvent(arena);
                }
            }
        }, 6000L, 6000L); // Every 5 minutes
        
        BedWars.plugin.getLogger().info("[Cosmic Chaos] Event handler enabled!");
    }
    
    @Override
    public void disable() {
        if (chaosTask != null) {
            chaosTask.cancel();
        }
        
        BedWars.plugin.getLogger().info("[Cosmic Chaos] Event handler disabled!");
    }
    
    @Override
    public void applyToArena(IArena arena) {
        arena.sendMessage("&d&lCosmic Chaos &7event is active! Expect the unexpected!");
        triggerRandomEvent(arena); // Trigger one immediately
    }
    
    @Override
    public void removeFromArena(IArena arena) {
        // No cleanup needed
    }
    
    @Override
    public void onPlayerJoin(Player player, IArena arena) {
        player.sendMessage("§8§m                                        ");
        player.sendMessage("§b§lThe Rift §8» §d§lCosmic Chaos Event");
        player.sendMessage("§7Random events every 5 minutes!");
        player.sendMessage("§7• Mystery supply drops");
        player.sendMessage("§7• 2x Crystal rewards");
        player.sendMessage("§8§m                                        ");
    }
    
    @Override
    public void onPlayerLeave(Player player, IArena arena) {
        // No cleanup needed
    }
    
    private void triggerRandomEvent(IArena arena) {
        int eventType = random.nextInt(5);
        
        switch (eventType) {
            case 0: // Speed boost for everyone
                arena.sendMessage("§d§lCosmic Chaos §8» §aSpeed boost for everyone!");
                for (Player player : arena.getPlayers()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
                }
                break;
                
            case 1: // Instant health
                arena.sendMessage("§d§lCosmic Chaos §8» §cInstant health for all!");
                for (Player player : arena.getPlayers()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1));
                }
                break;
                
            case 2: // Supply drop (random items)
                arena.sendMessage("§d§lCosmic Chaos §8» §6Mystery supply drop!");
                supplyDrop(arena);
                break;
                
            case 3: // Resistance
                arena.sendMessage("§d§lCosmic Chaos §8» §eResistance granted!");
                for (Player player : arena.getPlayers()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 0));
                }
                break;
                
            case 4: // Jump boost
                arena.sendMessage("§d§lCosmic Chaos §8» §bJump boost activated!");
                for (Player player : arena.getPlayers()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 400, 2));
                }
                break;
        }
    }
    
    private void supplyDrop(IArena arena) {
        // Drop items at random player locations
        for (Player player : arena.getPlayers()) {
            if (random.nextBoolean()) { // 50% chance per player
                Location loc = player.getLocation();
                World world = loc.getWorld();
                
                // Random items
                Material[] possibleItems = {
                    Material.DIAMOND,
                    Material.EMERALD,
                    Material.GOLD_INGOT,
                    Material.IRON_INGOT,
                    Material.ENDER_PEARL
                };
                
                Material item = possibleItems[random.nextInt(possibleItems.length)];
                int amount = random.nextInt(3) + 1;
                
                world.dropItemNaturally(loc, new ItemStack(item, amount));
                world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 20);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }
    
    private boolean isRotatingArena(IArena arena) {
        return arena.getGroup().equalsIgnoreCase("Rotating");
    }
}
```

---

## Testing Tips

### Test Event in Isolation
```java
// In your main class or a test command
DailyEventManager.getInstance().setCurrentEvent("WARP_SPEED");
```

### Debug Logging
Add this to each handler:
```java
BedWars.plugin.getLogger().info("[" + getEventName() + "] Applying to arena: " + arena.getArenaName());
```

### Check Event Application
```java
// In chat command or console
/bw debug event status
```

---

## Performance Notes

- **Scheduled Tasks**: Don't run too frequently (minimum 20 ticks / 1 second)
- **Potion Effects**: Use longer duration to reduce packet spam
- **Particle Effects**: Limit particle count to avoid lag
- **Falling Blocks**: Clean up after use to prevent entity buildup
- **World Time**: Only modify if necessary (Nebula Night)

---

## Next Steps

1. Start with **WarpSpeedHandler** - simplest implementation
2. Test thoroughly in development environment
3. Add **GravityShiftHandler** - also simple (just potion effects)
4. Add **NebulaNightHandler** - introduces world modification
5. Add **MeteorStormHandler** - introduces entity spawning
6. Add **CosmicChaosHandler** - introduces randomness
7. Implement remaining handlers based on complexity

Good luck! 🚀

