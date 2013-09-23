package com.bitlimit.NPCs;

import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.InteractBehavior;
import de.kumpelblase2.remoteentities.entities.RemotePlayer;
import de.kumpelblase2.remoteentities.persistence.serializers.JSONSerializer;
import de.kumpelblase2.remoteentities.persistence.serializers.YMLSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCs extends JavaPlugin implements Listener {
    public EntityManager manager;
    public NPCCommandExecutor commandExecutor;

    @Override
    public void onEnable() {
        try {
            this.manager = RemoteEntities.createManager(this);

            this.manager.setEntitySerializer(new YMLSerializer(this));
            this.manager.setSaveOnDisable(true);
            this.manager.loadEntities();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.commandExecutor = new NPCCommandExecutor(this);
        this.getCommand("npc").setExecutor(this.commandExecutor);

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    @Override
    public void saveConfig() {
        super.saveConfig();

        this.reloadConfig();
    }

    public void saveData() {
        this.manager.saveEntities();

    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity)event.getEntity();

        if (RemoteEntities.isRemoteEntity(livingEntity)) {
            event.setCancelled(true);

            RemoteEntity remoteEntity = RemoteEntities.getRemoteEntityFromEntity(livingEntity);
            if (remoteEntity instanceof RemotePlayer) {
                RemotePlayer remotePlayer = (RemotePlayer)remoteEntity;
                remotePlayer.fakeDamage();
                remotePlayer.doArmSwing();
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity)event.getRightClicked();
        if (RemoteEntities.isRemoteEntity(livingEntity)) {
            RemoteEntity remoteEntity = RemoteEntities.getRemoteEntityFromEntity(livingEntity);

            InteractBehavior interactBehavior = (InteractBehavior)remoteEntity.getMind().getBehaviour("Interact");
            if (interactBehavior instanceof BlacksmithInteractBehavior) {
                ((BlacksmithInteractBehavior)interactBehavior).onRightClickInteract(event.getPlayer());
            }
        }
    }

}
