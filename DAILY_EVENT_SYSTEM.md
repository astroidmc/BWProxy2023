# Daily Rotating Event System - Implementation Summary

## Overview
Implemented a complete daily rotating event system for "The Rift" gamemode. Events now rotate **once per day at midnight**, rather than multiple times throughout the day.

## Changes Made

### 1. New Files Created

#### **DailyEventManager.java**
- Location: `proxy-plugin/src/main/java/com/tomkeuper/bedwars/proxy/arenamanager/DailyEventManager.java`
- **Purpose**: Manages the daily event rotation system
- **Features**:
  - 7 unique daily events (Meteorite Storm, Gravity Shift, Cosmic Chaos, Solar Flare, Nebula Night, Asteroid Belt, Warp Speed)
  - Automatic rotation at midnight (scheduled task)
  - Redis integration to store current event (persists across server restarts)
  - Broadcast system to notify all players when event changes
  - Singleton pattern for easy access

#### **DailyEventCMD.java**
- Location: `proxy-plugin/src/main/java/com/tomkeuper/bedwars/proxy/command/main/DailyEventCMD.java`
- **Purpose**: Admin command to view daily event information
- **Usage**:
  - `/bw dailyevent info` - Show current event details
  - `/bw dailyevent current` - Show current event details
  - `/bw dailyevent list` - List all available events
- **Permission**: `bw.admin`

### 2. Modified Files

#### **RotatingEventGUI.java**
- Updated to display current daily event prominently at the top (Nether Star item)
- Shows all 7 available events in a grid layout
- Highlights the currently active event with glowstone dust
- 6-row inventory with better organization:
  - Row 1: Current event showcase
  - Row 2: Join button (beacon)
  - Rows 3-5: All available events
  - Row 6: Close button

#### **BedWarsProxy.java**
- Added initialization of DailyEventManager on server start (delayed by 70 ticks)
- Added shutdown hook to properly cancel the rotation task when plugin disables

#### **MainCommand.java**
- Registered the new `dailyevent` subcommand

#### **RedisConnection.java**
- Added `storeSetting(String key, String value)` method to persist event data in Redis
- Complements existing `retrieveSetting(String key)` method

### 3. Redis Storage Keys
- `daily_event_id` - Stores the ID of the current event
- `daily_event_date` - Stores the date when the current event was set

## How It Works

### Event Rotation
1. **On Server Start**: DailyEventManager checks Redis for stored event and date
2. **Date Comparison**: If the stored date is not today, a new random event is selected
3. **Automatic Rotation**: A scheduled task runs every 24 hours at midnight
4. **Player Notification**: When event changes, all online players receive a broadcast message

### Event Selection
- Random selection from 7 available events
- Each event has a unique ID, name, description, and feature list
- Events are stored in Redis to persist across server restarts

### GUI Integration
- Players can view the current event via `/bw gui rotating`
- The GUI shows:
  - **Top row**: Large showcase of today's event (Nether Star)
  - **Middle rows**: All 7 available events with descriptions
  - **Active event**: Highlighted with green color and glowstone dust material
  - **Future events**: Shown in gray with paper material

## The 7 Daily Events

1. **Meteorite Storm** (`METEOR_STORM`)
   - Meteorites rain from the sky
   - 2x Meteorite Shard drops
   - Random explosions

2. **Gravity Shift** (`GRAVITY_SHIFT`)
   - Altered gravity mechanics
   - Enhanced jump boost III
   - Easier bridging

3. **Cosmic Chaos** (`COSMIC_CHAOS`)
   - Random events every 5 minutes
   - 2x Crystal rewards
   - Mystery supply drops

4. **Solar Flare** (`SOLAR_FLARE`)
   - Fire damage in open areas
   - Safe zones near Rift Cores
   - Fire resistance upgrades

5. **Nebula Night** (`NEBULA_NIGHT`)
   - Permanent night time
   - Glowing ore deposits
   - Stealth gameplay bonus

6. **Asteroid Belt** (`ASTEROID_BELT`)
   - Floating asteroid platforms
   - Parkour challenges
   - New strategic routes

7. **Warp Speed** (`WARP_SPEED`)
   - Permanent Speed III
   - Faster mining & crafting
   - Haste II effect

## Commands Summary

| Command | Permission | Description |
|---------|-----------|-------------|
| `/bw gui rotating` | - | Open the daily event GUI |
| `/bw dailyevent info` | `bw.admin` | Show current event details |
| `/bw dailyevent list` | `bw.admin` | List all available events |

## Testing Checklist

- [ ] Server starts without errors
- [ ] DailyEventManager initializes correctly
- [ ] Current event is stored in Redis
- [ ] GUI opens and displays all events correctly
- [ ] Current event is highlighted in GUI
- [ ] Join button works (connects to "rotating" arena group)
- [ ] Admin command shows event information
- [ ] Event persists across server restarts
- [ ] Automatic rotation at midnight (may need to test manually)

## Future Enhancements

Consider implementing:
- Manual event override command for admins (`/bw dailyevent set <event_id>`)
- Event history tracking (last 7 days)
- Event schedule to ensure each event appears once per week
- Configuration file for event customization
- API events for other plugins to listen to event changes
- Integration with game servers to actually apply event modifiers

