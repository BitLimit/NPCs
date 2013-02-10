package com.kolinkrewinkel.BitLimitNPCs;

import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.persistence.EntityData;
import de.kumpelblase2.remoteentities.persistence.IEntitySerializer;
import de.kumpelblase2.remoteentities.persistence.serializers.JSONSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BitLimitNPCs extends JavaPlugin {
    public EntityManager manager;


    @Override
    public void onEnable() {
        try {
            this.manager = RemoteEntities.createManager(this);
            this.manager.setEntitySerializer(new JSONSerializer(this));
            this.manager.loadEntities();
        } catch (Exception e) {
            this.manager = null;

        }
        this.getCommand("npc").setExecutor(new NPCCommandExecutor(this));
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
