package com.raeden.dungeon_practice.commands;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import com.raeden.dungeon_practice.database.DatabaseFetcher;
import com.raeden.dungeon_practice.manager.DungeonManager;
import com.raeden.dungeon_practice.manager.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DungeonCommands implements CommandExecutor {

    private final DungeonGeneratorPractice dungeonPractice;

    private final DungeonManager dungeonManager;

    private final DatabaseFetcher database;

    private final SchematicManager schematicManager;


    // Constructor to accept DungeonManager
    public DungeonCommands(DungeonGeneratorPractice dungeonPractice, DungeonManager dungeonManager,
                           DatabaseFetcher database, SchematicManager schematicManager) {
        this.dungeonPractice = dungeonPractice;
        this.dungeonManager = dungeonManager;
        this.database = database;
        this.schematicManager = schematicManager;

    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        FileConfiguration config = dungeonPractice.getConfig();
        Set<String> dungeonTypes = config.getConfigurationSection("dungeon-types").getKeys(false);

        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (strings.length == 1) {
                if (strings[0].equalsIgnoreCase("create") || strings[0].equalsIgnoreCase("c")) {
                    p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon create [dungeon_type] [name]");
                }
                else if (strings[0].equalsIgnoreCase("config")) {
                    p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon config [reload]");
                    return true;
                }
                else if (strings[0].equalsIgnoreCase("teleport") || strings[0].equalsIgnoreCase("tp")) {
                    p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon teleport [name]");
                }
                else if (strings[0].equalsIgnoreCase("tpOv")) {
                    World overworld = Bukkit.getWorld("world"); // Default name for the overworld
                    Location ConfigSpawnLocation = dungeonManager.getConfigTeleportLocation();
                    if (ConfigSpawnLocation != null) {
                        p.teleport(ConfigSpawnLocation); // Teleports player to overworld's spawn location
                        p.sendMessage(ChatColor.GREEN + "Teleported to Spawn Location!");
                    } else {
                        p.teleport(overworld.getSpawnLocation());
                        p.sendMessage(ChatColor.RED + "Failed to find the Spawn Location! Teleported to default world spawn.");
                    }
                    return true;
                }
                else if (strings[0].equalsIgnoreCase("db-check") || strings[0].equalsIgnoreCase("db-chk")) {
                    p.sendMessage(ChatColor.RED + "Checking database connection...");
                    try {
                        if(database.isConnected()) {
                            p.sendMessage(ChatColor.GREEN + "Connected to a database!");
                            p.sendMessage(ChatColor.GREEN + "Database name: " + database.getDatabaseName());
                        }
                    } catch (SQLException e) {
                        p.sendMessage(ChatColor.RED + "Not connected to a database!");
                    }
                }
                else if (strings[0].equalsIgnoreCase("delete") || strings[0].equalsIgnoreCase("dlt")) {
                    p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon delete [name]");
                }
                else if (strings[0].equalsIgnoreCase("schematic") || strings[0].equalsIgnoreCase("sch")) {
                    p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon schematic [check|reload]");
                }
            }

            // String length = 2 meaning /dungeon [WHATEVER_HERE]
            if (strings.length == 2) {
                // Handle "create" or "c" command
                if (strings[0].equalsIgnoreCase("create") || strings[0].equalsIgnoreCase("c")) {
                    p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon create [dungeon_type] [name]");
                    return true;
                }
                // Handle "teleport" or "tp" command
                else if (strings[0].equalsIgnoreCase("teleport") || strings[0].equalsIgnoreCase("tp")) {
                    dungeonManager.teleportToDungeon(p, strings[1]);
                    return true;
                }
                else if (strings[0].equalsIgnoreCase("config") && strings[1].equalsIgnoreCase("reload")) {
                    dungeonPractice.reloadConfig(); // Reload the configuration
                    p.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
                    return true;
                }
                // Handle "delete" or "dlt" command
                else if ((strings[0].equalsIgnoreCase("delete") || strings[0].equalsIgnoreCase("dlt")) && !strings[1].equalsIgnoreCase("all")) {
                    dungeonManager.deleteDungeon(p, strings[1]);
                    return true;
                }
                else if ((strings[0].equalsIgnoreCase("delete") || strings[0].equalsIgnoreCase("dlt")) && strings[1].equalsIgnoreCase("all")) {
                    if (DungeonGeneratorPractice.generated_dungeon_count != null && !DungeonGeneratorPractice.generated_dungeon_count.isEmpty()) {
                        // Collect all dungeons into a temporary list to avoid concurrent modification issues
                        List<String> dungeonsToDelete = new ArrayList<>(DungeonGeneratorPractice.generated_dungeon_count);

                        for (String dungeonName : dungeonsToDelete) {
                            try {
                                // Attempt to delete each dungeon
                                dungeonManager.deleteDungeon(p, dungeonName);
                            } catch (Exception e) {
                                p.sendMessage("Failed to delete dungeon: " + dungeonName + ". Error: " + e.getMessage());
                            }
                        }

                        return true;
                    } else {
                        p.sendMessage("Failed to delete. No dungeons found!");
                    }
                }
                // Handle "schematic" or "sch" command
                else if (strings[0].equalsIgnoreCase("schematic") || strings[0].equalsIgnoreCase("sch")) {
                    if (strings[1].equalsIgnoreCase("check")) {
                        try {
                            p.sendMessage(ChatColor.GREEN + "Total schematics loaded: " + database.getSchematicCount());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        return true;
                    }
                    else if (strings[1].equalsIgnoreCase("reload")) {
                        p.sendMessage(ChatColor.GREEN + "Reloading schematics, check console.");
                        schematicManager.loadSchematics();
                        return true;
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon schematic [check|reload]");
                        return true;
                    }
                }
                // If none of the above matched, send error message
                else {
                    p.sendMessage(ChatColor.RED + "Wrong command usage! Please use: /dungeon [create|teleport|delete|schematic] [name]");
                    return true;
                }
            }
            // Length of 3
            if(strings.length == 3) {
                if (strings[0].equalsIgnoreCase("create") || strings[0].equalsIgnoreCase("c")) {
                    if (dungeonTypes == null || dungeonTypes.isEmpty()) {
                        p.sendMessage(ChatColor.RED + "Dungeon types are not configured properly!");
                        return true;
                    }

                    if (dungeonTypes.contains(strings[1])) {
                        if(strings[2] != null) {
                            dungeonManager.createDungeonWorld(p, strings[1], strings[2]);
                            return true;
                        } else {
                            p.sendMessage(ChatColor.RED + "Invalid dungeon name! Please use: /dungeon create [dungeon_type] [name]");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Invalid dungeon type! Please use: /dungeon create [dungeon_type] [name]");
                    }

                }
            }
        }
        return false;
    }
}
