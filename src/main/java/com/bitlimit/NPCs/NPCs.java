package com.bitlimit.NPCs;

import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.persistence.serializers.JSONSerializer;
import de.kumpelblase2.remoteentities.persistence.serializers.YMLSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCs extends JavaPlugin {
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

}
