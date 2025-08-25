package com.raeden.dungeon_practice.generators;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import com.raeden.dungeon_practice.database.DatabaseFetcher;
import com.raeden.dungeon_practice.manager.RPSchematicInfo;
import com.raeden.dungeon_practice.manager.SchematicInfo;
import com.raeden.dungeon_practice.utils.Coordinate;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DungeonRoomPicker {

    private final DungeonGeneratorPractice dungeonGeneratorPractice;
    private final HashMap<Coordinate, String> room_info;

    private final String worldName;

    private final Random random = new Random();

    private final DatabaseFetcher databaseConnection;


    public DungeonRoomPicker (DungeonGeneratorPractice dungeonGeneratorPractice, HashMap<Coordinate, String> room_info, DatabaseFetcher databaseConnection, String worldName) {
        this.dungeonGeneratorPractice = dungeonGeneratorPractice;
        this.worldName = worldName;
        this.room_info = room_info;
        this.databaseConnection = databaseConnection;

        pasteSchematics();
    }

    // Paste Schematic According to specific Direction
    public void pasteSchematics() {
        if (room_info == null) {
            Bukkit.getLogger().severe("No room information found to be pasted!");
            return;
        } else {
            for (Map.Entry<Coordinate, String> entry : room_info.entrySet()) {
                //System.out.println("FROM PASTE SCHEMATICS, ENTRY TEST: " + entry);
                int current_x = (entry.getKey().x) * 16;
                int current_z = (entry.getKey().y) * 16;
                String room_type = entry.getValue();

                // Split the room type and direction
                String[] parts = room_type.split("_", 2);
                String roomType = parts[0];
                String direction = parts.length > 1 ? parts[1] : "";

                System.out.println("DIRECTION: " + direction);


                // Fetch relevant schematics from the database
                List<RPSchematicInfo> schematics = databaseConnection.fetchSchematicsByDirection(direction);

                // Filter schematics based on room type
                if (roomType.equalsIgnoreCase("start")) {
                    schematics = filterSchematics(schematics, "start");
                } else if (roomType.equalsIgnoreCase("boss")) {
                    schematics = filterSchematics(schematics, "boss");
                }

                if (schematics.isEmpty()) {
                    Bukkit.getLogger().warning("No schematics found for direction: " + room_type);
                    return;
                }

                // Randomly select a schematic from the list
                RPSchematicInfo selectedSchematic = schematics.get(random.nextInt(schematics.size()));

                // Create the SchematicPaster instance
                DungeonSchematicPaster paster = new DungeonSchematicPaster(dungeonGeneratorPractice, 1);
                int y_level = 64;
                paster.pasteSchematic(selectedSchematic.getName(), selectedSchematic.getFilePath(), worldName, current_x, y_level, current_z);
            }



            databaseConnection.disconnect();
        }

    }

    // Paste Schematic According to specific Direction
    public void pasteSchematicsByRotating() {
        if (room_info == null) {
            Bukkit.getLogger().severe("No room information found to be pasted!");
            return;
        } else {
            for (Map.Entry<Coordinate, String> entry : room_info.entrySet()) {
                //System.out.println("FROM PASTE SCHEMATICS, ENTRY TEST: " + entry);
                int current_x = (entry.getKey().x) * 16;
                int current_z = (entry.getKey().y) * 16;
                String room_type = entry.getValue();

                // Split the room type and direction
                String[] parts = room_type.split("_", 2);
                String roomType = parts[0];
                String direction = parts.length > 1 ? parts[1] : "";


                // Fetch relevant schematics from the database
                List<RPSchematicInfo> schematics = databaseConnection.fetchSchematicsByDirection(direction);

                // Filter schematics based on room type
                if (roomType.equalsIgnoreCase("start")) {
                    schematics = filterSchematics(schematics, "start");
                } else if (roomType.equalsIgnoreCase("boss")) {
                    schematics = filterSchematics(schematics, "boss");
                }

                if (schematics.isEmpty()) {
                    Bukkit.getLogger().warning("No schematics found for direction: " + room_type);
                    return;
                }

                // Randomly select a schematic from the list
                RPSchematicInfo selectedSchematic = schematics.get(random.nextInt(schematics.size()));

                // Create the SchematicPaster instance
                DungeonSchematicPaster paster = new DungeonSchematicPaster(dungeonGeneratorPractice, 1);
                int y_level = 64;
                paster.pasteSchematic(selectedSchematic.getName(), selectedSchematic.getFilePath(), worldName, current_x, y_level, current_z);
            }



            databaseConnection.disconnect();
        }

    }

    private List<RPSchematicInfo> filterSchematics(List<RPSchematicInfo> schematics, String keyword) {
        List<RPSchematicInfo> filtered = new ArrayList<>();
        for (RPSchematicInfo schematic : schematics) {
            if (schematic.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(schematic);
            }
        }
        return filtered;
    }



}
