package com.kolinkrewinkel.BitLimitNPCs;

import com.google.common.base.Joiner;
import de.kumpelblase2.remoteentities.persistence.EntityData;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.kumpelblase2.remoteentities.*;
import de.kumpelblase2.remoteentities.api.*;
import de.kumpelblase2.remoteentities.api.features.*;
import de.kumpelblase2.remoteentities.api.thinking.*;
import de.kumpelblase2.remoteentities.api.thinking.goals.*;
import org.bukkit.entity.Damageable;
import de.kumpelblase2.remoteentities.entities.*;
import org.bukkit.entity.LivingEntity;
import net.minecraft.server.v1_4_R1.EntityHuman;

import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.block.Block;
import org.bukkit.util.*;
import org.bukkit.entity.Item;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class NPCCommandExecutor implements CommandExecutor, Listener {
    private final BitLimitNPCs plugin;
    private boolean editing = false;
    private ArrayList<String> npcNames = new ArrayList<String>();

    @EventHandler
    public void onDamageEvent(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof LivingEntity))
            return;

        if (plugin.manager.isRemoteEntity((LivingEntity)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    public NPCCommandExecutor(BitLimitNPCs plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Too few parameters.");
            return false;
        }

        if (args[0].toLowerCase().equals("create") && sender.hasPermission("npc.create")) {
            if (this.editing) {
                sender.sendMessage(ChatColor.RED + "Creation is not allowed during pruning.");
                return false;
            }

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
        } else if (args[0].toLowerCase().equals("prune") && sender.hasPermission("npc.prune"))
            this.setEditingWithSender(!this.editing, sender);
        else if (args[0].toLowerCase().equals("remove") && sender.hasPermission("npc.remove")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Entity ID required.");
                return false;
            }
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

        if (sender instanceof Player)
            player = (Player)sender;

        World firstWorld = Bukkit.getServer().getWorlds().get(0);
        Location spawnLocation = firstWorld.getSpawnLocation();
        RemotePlayer entity = (RemotePlayer)this.plugin.manager.createNamedEntity(RemoteEntityType.Human, sender instanceof Player ? player.getLocation() : firstWorld.getHighestBlockAt(spawnLocation).getLocation(), ChatColor.ITALIC + args[1]);

        entity.getMind().addMovementDesire(new DesireLookRandomly(entity), 1);
        entity.getMind().addMovementDesire(new DesireLookAtNearest(entity, EntityHuman.class, 16F, 1.0F), 2);
        entity.getMind().addBehaviour(new BlacksmithInteractBehavior(entity, this.plugin));
        this.plugin.saveData();

        Bukkit.broadcastMessage(ChatColor.WHITE + "<" + player.getDisplayName() + "> " + ChatColor.YELLOW + "A new blacksmith, dubbed " + ChatColor.AQUA + entity.getName() + ChatColor.RESET + ChatColor.YELLOW + ", has been synthesized on this fateful day.");
    }

    public void setEditingWithSender(boolean editing, CommandSender sender) {

        this.editing = editing;
        if (this.editing) {
            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + sender.getName() + "> " + ChatColor.YELLOW + "NPC pruning started…");
            npcNames.clear();

            for (RemoteEntity entity : this.plugin.manager.getAllEntities()) {
                if (entity instanceof RemotePlayer) {
                    RemotePlayer player  = (RemotePlayer)entity;
                    npcNames.add(player.getName());
                    player.setName(Integer.toString(player.getID()));
                }
            }
        } else {

            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + sender.getName() + "> " + ChatColor.GREEN + "NPC pruning completed.");

            for (RemoteEntity entity : this.plugin.manager.getAllEntities()) {
                if (entity instanceof RemotePlayer) {
                    ((RemotePlayer) entity).setName(this.npcNames.remove(0));
                }
            }
        }
    }
}
