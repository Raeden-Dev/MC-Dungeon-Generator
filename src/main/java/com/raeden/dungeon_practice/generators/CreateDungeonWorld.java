package com.raeden.dungeon_practice.generators;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/*
* This class currently handles the following operations:
1. Create a dungeon world Asynchronously (createDungeonWorldAsync)
*/


public class CreateDungeonWorld {
    // Check if a world with the given name exists
    private boolean doesWorldExist(String worldName) {
        return Bukkit.getWorld(worldName) != null;
    }

    public void createDungeonWorldAsync(JavaPlugin plugin, Player player, String dungeon_type, String worldName) {

        if (doesWorldExist(worldName)) {
            player.sendMessage(ChatColor.RED + "A dungeon with the name " + worldName + " already exists!");
        } else {
            // Run the world creation asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                WorldCreator myDungeonCreator = new WorldCreator(worldName);
                myDungeonCreator.environment(World.Environment.NORMAL);
                myDungeonCreator.generator(new ChunkGenerator() {
                    @Override
                    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
                        return createChunkData(world); // Generate void world
                    }
                });

                // Switch back to the main thread to generate the world and interact with Bukkit API
                Bukkit.getScheduler().runTask(plugin, () -> {
                    World dungeonWorld = myDungeonCreator.createWorld();

                    // Create world in a void-like state
                    CreateDungeonLayout dungeonLayout = new CreateDungeonLayout((DungeonGeneratorPractice) plugin, dungeon_type, worldName);
                    dungeonLayout.generateDungeonAsync();

                    if (dungeonWorld != null) {
                        // Set the spawn location and teleport player back on the main thread after world creation
                        Location spawnLocation = new Location(dungeonWorld, 0, 70, 0);
                        dungeonWorld.getBlockAt(spawnLocation).setType(Material.STONE); // Place starting block

                        dungeonWorld.setSpawnLocation(spawnLocation.add(0.5, 1, 0.5)); // Set default spawn point

                        DungeonGeneratorPractice.generated_dungeon_count.add(worldName);

                        // player.teleport(dungeonWorld.getSpawnLocation()); // Teleport player to spawn point
                        player.sendMessage(ChatColor.GREEN + "Created a dungeon world called " + worldName);
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to create dungeon world.");
                    }
                });
            });
        }
    }
}