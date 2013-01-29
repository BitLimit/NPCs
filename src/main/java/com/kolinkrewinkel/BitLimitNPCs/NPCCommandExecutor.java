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

public class NPCCommandExecutor implements CommandExecutor, Listener {
    private final BitLimitNPCs plugin;

    @EventHandler
    public void onDamageEvent(EntityDamageEvent event) {
        if (plugin.manager.isRemoteEntity((LivingEntity)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    public NPCCommandExecutor(BitLimitNPCs plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("npc.create")) {
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.RED + "Too few parameters.");
                return false;
            }

            if (args[0].toLowerCase().equals("create")) {
                try {
                    createNPCWithArguments(sender, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {

            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command.");
        }
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

        sender.sendMessage("Created NPC.");

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "More parameters required. Usage: ...");
            return;
        }

        Player player = null;

        if (sender instanceof Player) {
            player = (Player)sender;
        }

        World firstWorld = Bukkit.getServer().getWorlds().get(0);
        Location spawnLocation = firstWorld.getSpawnLocation();
        RemotePlayer entity = (RemotePlayer)this.plugin.manager.createNamedEntity(RemoteEntityType.Human, sender instanceof Player ? player.getLocation() : firstWorld.getHighestBlockAt(spawnLocation).getLocation(), ChatColor.ITALIC + args[1]);
        
        entity.setPushable(false);

        Player npc = (Player)entity.getBukkitEntity();
        npc.setCanPickupItems(false);
        npc.setRemoveWhenFarAway(false);
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
        npc.setItemInHand(axe);

        //Now we want him to look at the nearest player. This desire has a priority of 1 (the higher the better).
        //Since we don't have any other desires 1 is totally fine.
        //Note that when you have more than one desire with the same priority, both could get executed, but they'd need a different type (e.g. looking and moving)
        //This does not get executed all the time. It might just get executed but he might take a break for 2 seconds after that. It's random.
        entity.getMind().addMovementDesire(new DesireLookRandomly(entity), 1);
        entity.getMind().addMovementDesire(new DesireLookAtNearest(entity, EntityHuman.class, 16F, 1.0F), 2);

        entity.getMind().addBehaviour(new BlacksmithInteractBehavior(entity, this.plugin));

        this.plugin.saveData();
    }
}
