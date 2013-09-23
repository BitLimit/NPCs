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
//            this.manager = RemoteEntities.createManager(this);
//            this.manager.setEntitySerializer(new YMLSerializer(this));
//            this.manager.saveEntities();
//
//            this.manager.setSaveOnDisable(true);
//            this.manager.loadEntities();

            this.manager = RemoteEntities.createManager(this);

            //First we register the serializer that should be used
            //In this case we use the YML serializer, but you can also use your own as well as the json serializer
            this.manager.setEntitySerializer(new YMLSerializer(this));
            //To load the entities we saved, you can just do this:
            this.manager.loadEntities();

            //If we want to save all current entities, it's pretty easy:
//            this.manager.saveEntities();
//
//            //Some serializers allow single entities to be saved, i.e. the YML serializer
//            //But first, we need to create an entitiy.
//            RemoteEntity entity = this.manager.createNamedEntity(RemoteEntityType.Human, Bukkit.getWorld("world").getSpawnLocation(), "Smith");
//            //Now we can just call the save method. Keep in mind that it will not work when the serializer doesn't support single entity serialization
//            entity.save();

                        //Now all the entities should be back like you never removed them
        } catch (Exception e) {
            this.manager = null;
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
