package com.kolinkrewinkel.BitLimitNPCs;

import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.Behavior;
import de.kumpelblase2.remoteentities.api.thinking.InteractBehavior;
import de.kumpelblase2.remoteentities.entities.RemotePlayer;
import de.kumpelblase2.remoteentities.persistence.ParameterData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class BlacksmithInteractBehavior extends InteractBehavior {
    private Plugin plugin;

    public BlacksmithInteractBehavior(RemoteEntity inEntity, Plugin inPlugin) {
        super(inEntity);
        this.plugin = inPlugin;

        this.m_entity.setPushable(false);

        Player npc = (Player)this.m_entity.getBukkitEntity();
        npc.setCanPickupItems(false);
        npc.setRemoveWhenFarAway(false);
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
        npc.setItemInHand(axe);
    }

    public BlacksmithInteractBehavior(ParameterData[] parameters)
    {
        this((RemoteEntity)parameters[0].value, (Plugin)parameters[1].value);
    }


    @Override
    public void onInteract(Player inPlayer) {
        RemotePlayer behaviorEntity = (RemotePlayer) this.getRemoteEntity();
        Player npc = (Player) behaviorEntity.getBukkitEntity();
        Location npcLocation = npc.getLocation();

        //example usage
        // Bukkit.getServer().broadcastMessage(Integer.toString(npc.getItemInHand().getType().getId()));
        ItemStack repairItem = inPlayer.getItemInHand();

        if (npc.getItemInHand().getType().getId() != 279) {
            inPlayer.sendMessage(ChatColor.AQUA + npc.getDisplayName() + ChatColor.RED + " is busy!");
            return;
        } else if (repairItem.getMaxStackSize() != 1) {
            return;
        } else if (repairItem.getDurability() == 0) {
            inPlayer.sendMessage(ChatColor.RED + "Your item is fully repaired.");
            return;
        }


        npc.setItemInHand(repairItem);

        int indexOfPreviouslyHeldItem = inPlayer.getInventory().getHeldItemSlot();
        inPlayer.setItemInHand(new ItemStack(Material.AIR));
        //Update the equipment on the entity.


        Block closest = closestBlock(npcLocation, Material.ANVIL.getId());

        double distanceAway = npcLocation.distance(closest.getLocation());
        // Bukkit.getServer().broadcastMessage("Found distance...");
        behaviorEntity.move(closest.getLocation());

        class FinishRepairTrip implements Runnable {
            private final Player npc;
            private final Location gaze;

            public FinishRepairTrip(Player npc, Location gaze) {
                this.npc = npc;
                this.gaze = gaze;
            }

            public void run() {
                npc.setSneaking(true);
                // npc.teleport(lookAt(npc.getLocation(), gaze));

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        npc.setSneaking(false);

                        npc.getWorld().playSound(npc.getLocation(), Sound.ANVIL_USE, 1F, 1F);
                    }
                }, 20L);
            }
        }

        Long wait = Math.round((distanceAway / 4) * 20);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new FinishRepairTrip(npc, closest.getLocation()), wait);

        short zero = 0;
        repairItem.setDurability(zero);

        class ReturnNPC implements Runnable {
            private final Location returnLocation;
            private final RemotePlayer entity;

            public ReturnNPC(Location returnLocation, RemotePlayer entity) {
                this.returnLocation = returnLocation;
                this.entity = entity;
            }

            public void run() {
                this.entity.move(returnLocation);
            }
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ReturnNPC(npcLocation, behaviorEntity), wait + 20L);


        class ReturnItem implements Runnable {
            private final Player player;
            private final ItemStack item;
            private final Player npc;
            private final int attemptSlot;

            public ReturnItem(Player player, ItemStack item, Player npc, int attemptSlot) {
                this.player = player;
                this.item = item;
                this.npc = npc;
                this.attemptSlot = attemptSlot;
            }

            public void run() {
                if (this.player.getInventory().getItem(this.attemptSlot) == null && this.player.isOnline()) {
                    this.player.getInventory().setItem(this.attemptSlot, this.item);
                } else if (this.player.getInventory().firstEmpty() != -1 && this.player.isOnline()) {
                    this.player.getInventory().setItem(this.player.getInventory().firstEmpty(), this.item);
                } else {
                    Item droppedItem = npc.getLocation().getWorld().dropItem(npc.getLocation(), this.item);
                    Location playerLoc = this.player.getLocation();
                    droppedItem.setVelocity(new Vector(0, 0, 0));
                }


                if (item.getItemMeta().hasDisplayName()) {
                    this.player.sendMessage(ChatColor.AQUA + this.npc.getDisplayName() + ChatColor.GREEN + " repaired " + ChatColor.GOLD + item.getItemMeta().getDisplayName());
                } else {
                    this.player.sendMessage(ChatColor.AQUA + this.npc.getDisplayName() + ChatColor.GREEN + " repaired your " + ChatColor.GOLD + item.getType().name().replace("_", " ").toLowerCase());
                }


                ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
                npc.setItemInHand(axe);

            }
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ReturnItem(inPlayer, repairItem, npc, indexOfPreviouslyHeldItem), (wait * 2) + 20L);
    }


    private String capitalizedString(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    private Block closestBlock(Location origin, Integer type) {
        World world = origin.getWorld();

        for (int x = origin.getBlockX() - 4; x <= origin.getBlockX() + 8; x = x + 1) {
            for (int z = origin.getBlockZ() - 4; z <= origin.getBlockZ() + 8; z = z + 1) {
                for (int y = origin.getBlockY() - 4; y <= origin.getBlockY() + 8; y = y + 1) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.ANVIL) {
                        return block;
                    }
                }
            }
        }


        return world.getBlockAt(origin);
    }

    @Override
    public Object[] getConstructionals()
    {
        Object[] constructionals = new Object[2];
        constructionals[0] = this.m_entity;
        constructionals[1] = this.plugin;

        return constructionals;
    }
}
