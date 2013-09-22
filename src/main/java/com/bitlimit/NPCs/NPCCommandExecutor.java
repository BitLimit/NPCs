package com.bitlimit.NPCs;

import de.kumpelblase2.remoteentities.RemoteEntities;
import net.minecraft.server.v1_6_R3.EntityHuman;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import de.kumpelblase2.remoteentities.api.*;
import de.kumpelblase2.remoteentities.api.thinking.goals.*;
import de.kumpelblase2.remoteentities.entities.*;
import org.bukkit.entity.LivingEntity;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class NPCCommandExecutor implements CommandExecutor, Listener {
    private final NPCs plugin;
    private HashMap<RemoteEntity, String> NPCNames = new HashMap<RemoteEntity, String>();

    public NPCCommandExecutor(NPCs plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Too few parameters.");
            return false;
        }

        if (args[0].toLowerCase().equals("create") && sender.hasPermission("npc.create")) {

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Name required.");
                return false;
            }

            try {
                createNPCWithArguments(sender, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        else if (args[0].toLowerCase().equals("remove") && sender.hasPermission("npc.remove")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Entity ID required.");
                return false;
            }

            int ID = Integer.parseInt(args[1]);
            RemoteEntity entity = this.plugin.manager.getRemoteEntityByID(ID);

            this.NPCNames.remove(entity);
            this.plugin.manager.removeEntity(ID, true);

            sender.sendMessage(ChatColor.GREEN + "Entity of ID \"" + ID + "\" removed.");
        } else
            sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command.");

        return false;
    }

    private boolean isValidBooleanInput(String string) {
        return string.equals("enable") || string.equals("enabled") || string.equals("true") || string.equals("YES") || string.equals("yes") || string.equals("disable") || string.equals("disabled") || string.equals("false") || string.equals("NO") || string.equals("no");
    }

    private boolean parsedBooleanInput(String string) {
        if (string.equals("enable") || string.equals("enabled") || string.equals("true") || string.equals("YES") || string.equals("yes")) {
            return true;
        } else if (string.equals("disable") || string.equals("disabled") || string.equals("false") || string.equals("NO") || string.equals("no")) {
            return false;
        }
        return false;
    }

    private void createNPCWithArguments(CommandSender sender, String[] args) throws Exception {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "More parameters required. Usage: ... (not yet defined.)");
            return;
        }

        Player player = null;

        Location toSpawnLocation;
        if (sender instanceof Player) {
            player = (Player)sender;
            toSpawnLocation = player.getLocation();
        } else {
            World firstWorld = Bukkit.getServer().getWorlds().get(0);
            Location worldSpawn = firstWorld.getSpawnLocation();
            toSpawnLocation = firstWorld.getHighestBlockAt(worldSpawn).getLocation();
        }

        // Create the NPC.
        RemotePlayer entity = (RemotePlayer)this.plugin.manager.createNamedEntity(RemoteEntityType.Human, toSpawnLocation, ChatColor.ITALIC + args[1] + ChatColor.RESET, true);

        // Set up desires and behaviors (the fairy dust.)
        entity.getMind().addMovementDesire(new DesireLookRandomly(), 1);
        entity.getMind().addMovementDesire(new DesireLookAtNearest(EntityHuman.class, 16F, 1.0F), 2);
        entity.getMind().addBehaviour(new BlacksmithInteractBehavior(entity));
        entity.getMind().blockFeelings(false);

        Bukkit.broadcastMessage(ChatColor.WHITE + "<" + player.getDisplayName() + "> " + ChatColor.YELLOW + "A new blacksmith, dubbed " + ChatColor.AQUA + entity.getName() + ChatColor.YELLOW + ", has been synthesized on this fateful day.");

        entity.save();
        this.plugin.manager.saveEntities();
    }


}
