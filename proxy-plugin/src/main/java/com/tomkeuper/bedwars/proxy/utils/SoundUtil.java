package com.tomkeuper.bedwars.proxy.utils;

import com.tomkeuper.bedwars.proxy.BedWarsProxy;
import org.bukkit.Sound;

public class SoundUtil {
    public static Sound getForCurrentVersion(String v1_8, String v1_12, String v1_13) {
        int version = VersionUtil.getMajorMinecraftVersion();

        String soundName;
        if (version <= 112) {
            soundName = (version <= 108) ? v1_8 : v1_12;
        } else {
            // For 1.13+ use the modern sound name
            soundName = v1_13;
        }

        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            BedWarsProxy.getPlugin().getLogger().warning("Invalid sound name for version " + version + ": " + soundName + ". Attempting fallback...");

            // Try fallback sounds in case the provided sound doesn't exist
            try {
                if (soundName.equals("ENTITY_VILLAGER_NO")) {
                    return Sound.valueOf("ENTITY_VILLAGER_NO");
                } else if (soundName.equals("ENTITY_SLIME_JUMP")) {
                    return Sound.valueOf("ENTITY_SLIME_SQUISH");
                } else if (soundName.equals("ENTITY_CHICKEN_EGG")) {
                    return Sound.valueOf("ENTITY_CHICKEN_EGG");
                }
            } catch (IllegalArgumentException ex) {
                // Ignore fallback failures
            }

            return null;
        }
    }
}
