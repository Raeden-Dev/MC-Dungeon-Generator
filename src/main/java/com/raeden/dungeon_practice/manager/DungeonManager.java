package com.raeden.dungeon_practice.manager;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import com.raeden.dungeon_practice.generators.CreateDungeonWorld;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.bukkit.Bukkit.getLogger;

/*
* This class currently handles the following operations:
1. Create a dungeon world (createDungeonWorld)
    - Children:
    1.1: getConfigTeleportLocation (GETS TELEPORT LOCATION FROM CONFIG)
    1.2: doesWorldExist (CHECKS IF THE WORLD EXISTS
    * Line.71: Creates an instance of CreateDungeonWorld to make the world
2. Teleport to dungeon world (teleportToDungeon)
3. Delete a dungeon world (deleteDungeon)
    - Children:
    3.1: deleteDirectory (CHECKS A WHOLE DIRECTORY AND DELETES FILE ONE BY ONE)
*/


public class DungeonManager {

    public String default_teleportWorldName = "world";

    private final DungeonGeneratorPractice plugin; // Add JavaPlugin instance

    // Constructor to initialize the plugin
    public DungeonManager(DungeonGeneratorPractice plugin) {
        this.plugin = plugin;
    }

    // Method to retrieve default teleport location
    public Location getConfigTeleportLocation() {
        FileConfiguration config = plugin.getConfig();

        String worldName = config.getString("default-teleport-location.world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().warning("World '" + worldName + "' not found!");
            return null;
        }

        double x = config.getDouble("default-teleport-location.x");
        double y = config.getDouble("default-teleport-location.y");
        double z = config.getDouble("default-teleport-location.z");
        float yaw = (float) config.getDouble("default-teleport-location.yaw");
        float pitch = (float) config.getDouble("default-teleport-location.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }


    // Check if a world with the given name exists
    public boolean doesWorldExist(String worldName) {
        return Bukkit.getWorld(worldName) != null;
    }

    // Create a dungeon world if it doesn't exist
    public void createDungeonWorld(Player player, String dungeon_type, String worldName) {
        if (doesWorldExist(worldName)) {
            player.sendMessage(ChatColor.RED + "A dungeon with the name " + worldName + " already exists!");
        } else {
            new CreateDungeonWorld().createDungeonWorldAsync(plugin, player, dungeon_type, worldName);
        }
    }

    // Teleport to a dungeon world if it exists
    public void teleportToDungeon(Player player, String worldName) {
        if (!doesWorldExist(worldName)) {
            player.sendMessage(ChatColor.RED + "The dungeon " + worldName + " does not exist!");
        } else {
            World dungeonWorld = Bukkit.getWorld(worldName);
            if (dungeonWorld != null) {
                player.teleport(dungeonWorld.getSpawnLocation());
                player.sendMessage(ChatColor.GREEN + "Teleported to dungeon world " + worldName);
            }
        }
    }
    // Method to delete the dungeon directory and send messages to the player
    public void deleteDungeon(Player player, String dungeonName) {
        World dungeonWorld = Bukkit.getWorld(dungeonName); // Get the world instance
        File dungeonDir = new File(dungeonName); // Adjust the path as needed

        if (dungeonDir.exists()) {
            // Teleport all players in the dungeon world to the specified world
            if (dungeonWorld != null) {
                DungeonGeneratorPractice.generated_dungeon_count.remove(dungeonName);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getWorld().equals(dungeonWorld)) {
                        // Teleport to the specified world
                        World default_teleportWorld = Bukkit.getWorld(default_teleportWorldName);
                        Location config_teleport_location = getConfigTeleportLocation();
                        if (config_teleport_location != null) {
                            onlinePlayer.teleport(config_teleport_location);
                            onlinePlayer.sendMessage(ChatColor.RED + "The dungeon " + dungeonName + " has been deleted. You have been teleported out.");
                        } else {
                            onlinePlayer.teleport(default_teleportWorld.getSpawnLocation());
                            onlinePlayer.sendMessage(ChatColor.RED + "Error: Config Teleport location not found. Teleporting to default world spawn.");
                        }
                    }
                }
            }

            try {
                deleteDirectory(dungeonDir);
                // Unload the dungeon world
                if (dungeonWorld != null) {
                    Bukkit.unloadWorld(dungeonWorld, false);
                }
                // Send success message to the player who initiated the deletion
                player.sendMessage(ChatColor.GREEN + "Dungeon " + dungeonName + " deleted successfully.");
                Bukkit.getLogger().info("Dungeon " + dungeonName + " was deleted by " + player.getName());
            } catch (IOException e) {
                // Send error message to the player who initiated the deletion
                player.sendMessage(ChatColor.RED + "Failed to delete dungeon " + dungeonName + ": " + e.getMessage());
            }
        } else {
            // Send warning message to the player who initiated the deletion
            player.sendMessage(ChatColor.RED + "Dungeon " + dungeonName + " does not exist.");
        }
    }

    // Recursive method to delete directory and its contents
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file); // Recursively delete contents
                }
            }
        }
        // Finally, delete the empty directory or file
        Files.delete(directory.toPath());
    }
}