package com.raeden.dungeon_practice;

import com.raeden.dungeon_practice.commands.DungeonCommands;
import com.raeden.dungeon_practice.commands.DungeonTablist;
import com.raeden.dungeon_practice.database.DatabaseFetcher;
import com.raeden.dungeon_practice.manager.DungeonManager;
import com.raeden.dungeon_practice.manager.SchematicManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DungeonGeneratorPractice extends JavaPlugin {

    public static List<String> generated_dungeon_count = new ArrayList<>();

    private DungeonManager dungeonManager;
    private DatabaseFetcher database;
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        reloadConfig();

        dungeonManager = new DungeonManager(this);
        database = new DatabaseFetcher(this);

        try {
            database.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        File dungeonSchematicFolder = new File(getDataFolder(), "dungeons");
        SchematicManager schematicManager = new SchematicManager(this, dungeonSchematicFolder, getLogger(), database);

        // Plugin startup logic
        getCommand("dungeon").setExecutor(new DungeonCommands(this, dungeonManager, database, schematicManager));
        getCommand("dungeon").setTabCompleter(new DungeonTablist(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.disconnect();
    }
}
