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
    public NPCCommandExecutor commandExecutor;

    @Override
    public void onEnable() {
        try {
            this.manager = RemoteEntities.createManager(this);
            this.manager.setEntitySerializer(new JSONSerializer(this));
            this.manager.loadEntities();
        } catch (Exception e) {
            this.manager = null;

        }

        this.commandExecutor = new NPCCommandExecutor(this);
        this.getCommand("npc").setExecutor(this.commandExecutor);
    }

    @Override
    public void onDisable() {
        if (this.commandExecutor.editing)
            this.commandExecutor.setEditingWithSender(false, Bukkit.getConsoleSender());

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
