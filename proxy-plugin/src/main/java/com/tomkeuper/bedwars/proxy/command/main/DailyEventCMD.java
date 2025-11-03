package com.tomkeuper.bedwars.proxy.command.main;

import com.astroid.bedwars.proxy.api.command.SubCommand;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.tomkeuper.bedwars.proxy.arenamanager.DailyEventManager;
import com.tomkeuper.bedwars.proxy.arenamanager.DailyEventManager.DailyEvent;
import org.bukkit.command.CommandSender;

/**
 * Admin command to manage daily events.
 * Usage: /bw dailyevent [info|current]
 */
public class DailyEventCMD extends SubCommand {

    /**
     * Create a new sub-command for daily event management.
     *
     * @param name       sub-command name.
     * @param permission sub-command permission.
     */
    public DailyEventCMD(String name, String permission) {
        super(name, permission);
    }

    @Override
    public void execute(CommandSender s, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("current")) {
            // Show current event info
            DailyEvent currentEvent = DailyEventManager.getInstance().getCurrentEvent();

            s.sendMessage("§8§m                                                    ");
            s.sendMessage("§b§lThe Rift §8» §6§lDaily Event System");
            s.sendMessage("");
            s.sendMessage("§7Current Event: §e§l" + currentEvent.getName());
            s.sendMessage("§7Event ID: §f" + currentEvent.getId());
            s.sendMessage("");
            s.sendMessage("§e§lDescription:");
            for (String line : currentEvent.getDescription()) {
                s.sendMessage("  §7" + IridiumColorAPI.process(line));
            }
            s.sendMessage("");
            s.sendMessage("§d§lFeatures:");
            for (String feature : currentEvent.getFeatures()) {
                s.sendMessage("  §7• §d" + IridiumColorAPI.process(feature));
            }
            s.sendMessage("");
            s.sendMessage("§7This event is active for the entire day.");
            s.sendMessage("§7Next rotation: §eTonight at midnight");
            s.sendMessage("§8§m                                                    ");
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            // Show all available events
            s.sendMessage("§8§m                                                    ");
            s.sendMessage("§b§lThe Rift §8» §6§lAll Daily Events");
            s.sendMessage("");

            DailyEvent current = DailyEventManager.getInstance().getCurrentEvent();

            for (DailyEvent event : DailyEventManager.getInstance().getAllEvents()) {
                boolean isCurrent = event.getId().equals(current.getId());
                String prefix = isCurrent ? "§a§l✓ §e" : "§7• §f";
                String suffix = isCurrent ? " §a§l(ACTIVE)" : "";
                s.sendMessage(prefix + event.getName() + suffix);
                s.sendMessage("  §7ID: §f" + event.getId());
            }

            s.sendMessage("");
            s.sendMessage("§7Use §e/bw dailyevent info §7for current event details");
            s.sendMessage("§8§m                                                    ");
            return;
        }

        // Unknown argument
        s.sendMessage("§cUsage: /bw dailyevent [info|current|list]");
    }
}

