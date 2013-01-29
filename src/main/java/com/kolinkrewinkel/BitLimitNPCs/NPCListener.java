package com.kolinkrewinkel.BitLimitNPCs;

import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;

import java.util.*;

public class NPCListener implements Listener {
    private final BitLimitNPCs plugin; // Reference main plugin

    /*********************************************
          Initialization: NPCListener(plugin)
    ----------- Designated Initializer ----------
    *********************************************/

    public NPCListener(BitLimitNPCs plugin) {
        // Notify plugin manager that this plugin handles implemented events (block place, etc.)
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
}
