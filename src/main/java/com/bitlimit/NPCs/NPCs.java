package com.bitlimit.NPCs;

import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.InteractBehavior;
import de.kumpelblase2.remoteentities.persistence.serializers.YMLSerializer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCs extends JavaPlugin implements Listener {
    public EntityManager manager;
    public NPCCommandExecutor commandExecutor;

    @Override
    public void onEnable() {
        try {
            this.manager = RemoteEntities.createManager(this, false);

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
    public void onPlayerInteractWithEntityEvent(PlayerInteractEntityEvent interactEvent)
    {
        if (!(interactEvent.getRightClicked() instanceof LivingEntity))
        {
            return;
        }

        LivingEntity livingEntity = (LivingEntity)interactEvent.getRightClicked();

        if (RemoteEntities.isRemoteEntity(livingEntity))
        {

            RemoteEntity remoteEntity = RemoteEntities.getRemoteEntityFromEntity(livingEntity);

            if (remoteEntity.getMind().hasBehaviour("Interact"))
            {
                InteractBehavior interactBehavior = (InteractBehavior)remoteEntity.getMind().getBehaviour("Interact");
                if (interactBehavior instanceof BlacksmithInteractBehavior) {
                    BlacksmithInteractBehavior blacksmithInteractBehavior = (BlacksmithInteractBehavior)interactBehavior;
                    blacksmithInteractBehavior.onRightClickInteractEventWithPlayer(interactEvent.getPlayer());
                }
            }
        }
    }
}
