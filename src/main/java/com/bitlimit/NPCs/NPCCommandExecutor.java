package com.bitlimit.NPCs;

import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntityType;
import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookAtNearest;
import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookRandomly;
import de.kumpelblase2.remoteentities.entities.RemotePlayer;
import net.minecraft.server.v1_6_R3.EntityHuman;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class NPCCommandExecutor implements CommandExecutor, Listener {
    private final NPCs plugin;

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

            this.plugin.manager.removeEntity(ID, true);

            sender.sendMessage(ChatColor.GREEN + "Entity of ID \"" + ID + "\" removed.");
        } else if (args[0].toLowerCase().equals("tp") && sender.hasPermission("npc.tp")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Entity ID required.");
                return false;
            }

            if (args.length < 5) {
                sender.sendMessage(ChatColor.RED + "Coordinates required.");
                return false;
            }

            if (args.length < 6) {
                sender.sendMessage(ChatColor.RED + "World required.");
                return false;
            }

            int ID = Integer.parseInt(args[1]);
            double x = Double.parseDouble(args[2]);
            double y = Double.parseDouble(args[3]);
            double z = Double.parseDouble(args[4]);

            RemoteEntity entity = this.plugin.manager.getRemoteEntityByID(ID);
            entity.teleport(new Location(Bukkit.getWorld(args[5]), x, y, z));
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

        if (sender instanceof Player) {
            Player player = (Player)sender;
            Location toSpawnLocation = player.getLocation();

            if (args[1].length() > 16) {
                sender.sendMessage(ChatColor.RED + "Name exceeds maximum length.");
                return;
            }

            // Create the NPC.
            RemotePlayer entity = (RemotePlayer)this.plugin.manager.createNamedEntity(RemoteEntityType.Human, toSpawnLocation, ChatColor.ITALIC + args[1] + ChatColor.RESET, true);

            // Set up desires and behaviors (the fairy dust.)
            entity.getMind().addMovementDesire(new DesireLookRandomly(), 1);
            entity.getMind().addMovementDesire(new DesireLookAtNearest(EntityHuman.class, 8F, 1.0F), 2);
            entity.getMind().addBehaviour(new BlacksmithInteractBehavior(entity));

            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + player.getDisplayName() + "> " + ChatColor.YELLOW + "A new blacksmith, dubbed " + ChatColor.AQUA + entity.getName() + ChatColor.YELLOW + ", has been synthesized on this fateful day.");

            this.plugin.saveData();
        }
    }


}
