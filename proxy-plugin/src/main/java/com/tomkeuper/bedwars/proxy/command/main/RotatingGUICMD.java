package com.tomkeuper.bedwars.proxy.command.main;

import com.astroid.bedwars.proxy.api.command.SubCommand;
import com.tomkeuper.bedwars.proxy.arenamanager.RotatingEventGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Subcommand to open the Rotating Event GUI.
 * Usage: /bw gui rotating
 */
public class RotatingGUICMD extends SubCommand {

    /**
     * Create a new sub-command for rotating event GUI.
     *
     * @param name       sub-command name.
     * @param permission sub-command permission, leave empty if no permission is required.
     */
    public RotatingGUICMD(String name, String permission) {
        super(name, permission);
    }

    @Override
    public void execute(CommandSender s, String[] args) {
        if (s instanceof ConsoleCommandSender) return;
        Player p = (Player) s;

        RotatingEventGUI.openRotatingEventGUI(p);
    }
}

