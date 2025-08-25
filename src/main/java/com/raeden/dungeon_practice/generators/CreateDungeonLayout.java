package com.raeden.dungeon_practice.generators;

import java.sql.SQLException;
import java.util.*;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import com.raeden.dungeon_practice.database.DatabaseFetcher;
import com.raeden.dungeon_practice.utils.Coordinate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import javax.xml.crypto.Data;

public class CreateDungeonLayout {
    private final DungeonGeneratorPractice dungeonGeneratorPractice;



    private final FileConfiguration config;

    private final String dungeon_type;

    private final String worldName;

    private final Random random = new Random();

    private int total_dungeon_room;
    private int total_boss_room;
    private int current_boss_room;

    private int dungeon_current_room = 0;
    private int dungeon_current_x = 0;
    private int dungeon_current_y = 0;

    // Retry mechanism limit
    private final int maxRetries = 3;
    private int currentRetries = 0;
    private final HashMap<String, Coordinate> directions = new HashMap<>();

    private final HashMap<Coordinate, String> room_info = new HashMap<>();

    private final HashMap<Coordinate, String> room_info_unsorted = new HashMap<>();

    public CreateDungeonLayout (DungeonGeneratorPractice dungeonPractice, String dungeon_type, String worldName) {
        this.worldName = worldName;
        this.dungeon_type = dungeon_type;
        this.dungeonGeneratorPractice = dungeonPractice;

        this.config = dungeonGeneratorPractice.getConfig();

        directions.put("N", new Coordinate(0, 1));
        directions.put("S", new Coordinate(0, -1));
        directions.put("E", new Coordinate(1, 0));
        directions.put("W", new Coordinate(-1, 0));
        loadRoomInfo();
    }

    public void loadRoomInfo () {

        if (dungeon_type == null) {
            Bukkit.getLogger().severe("Problem getting dungeon name from config!");
            return;
        }

        int minDungeonRooms = config.getInt("dungeon-types." + dungeon_type + ".dungeon-rooms.min");
        int maxDungeonRooms = config.getInt("dungeon-types." + dungeon_type + ".dungeon-rooms.max");

        this.total_dungeon_room = random.nextInt((maxDungeonRooms - minDungeonRooms) + 1) + minDungeonRooms;

        int minBossRooms = config.getInt("dungeon-types." + dungeon_type + ".dungeon-rooms.boss-rooms-min");
        int maxBossRooms = config.getInt("dungeon-types." + dungeon_type + ".dungeon-rooms.boss-rooms-max");

        this.total_boss_room = random.nextInt((maxBossRooms - minBossRooms) + 1) + minBossRooms;

        current_boss_room = total_boss_room;

    }
    public void tellInfo() {
        System.out.println("_______________________ DUNGEON INFO _______________________");
        System.out.println("TOTAL BOSS ROOM: " + total_boss_room);
        System.out.println("TOTAL ROOM: " + total_dungeon_room);
        System.out.println("DUNGEON TYPE: " + dungeon_type);
        System.out.println("Room Hashmap: \n" + room_info);
        System.out.println("Unsorted Room Hashmap: \n" + room_info_unsorted);
        System.out.println("____________________________________________________________");

    }

    public List<String> getAvailableDirections() {
        List<String> availableDirections = new ArrayList<>();
        for (Map.Entry<String, Coordinate> entry : directions.entrySet()) {
            int newX = dungeon_current_x + entry.getValue().x;
            int newY = dungeon_current_y + entry.getValue().y;
            Coordinate newCoord = new Coordinate(newX, newY);
            if (!room_info.containsKey(newCoord)) {
                availableDirections.add(entry.getKey());
            }
        }
        return availableDirections;
    }



    public String getOppositeDirection(String direction) {
        HashMap<String, String> oppositeDirections = new HashMap<>();
        oppositeDirections.put("N", "S");
        oppositeDirections.put("S", "N");
        oppositeDirections.put("E", "W");
        oppositeDirections.put("W", "E");
        return oppositeDirections.get(direction);
    }

    public boolean restart_check() {
        int randomRestart = random.nextInt(100) + 1;
        int restart_chance = config.getInt("dungeon-types." + dungeon_type + ".dungeon-rooms.restart-chance");
        return randomRestart <= restart_chance;
    }

    public void add_room(int x, int y, String room_type) {
        Coordinate coord = new Coordinate(x, y);

        if (room_info.containsKey(coord)) {
            return; // Room already exists, exit the method
        }

        dungeon_current_room += 1;

        // Start with the room type as the base of the connection string
        StringBuilder connections = new StringBuilder(room_type + "_"); // Use underscore to denote connections

        StringBuilder connectionDirections = new StringBuilder();

        // Check neighboring rooms for connections and update their connection strings
        for (Map.Entry<String, Coordinate> entry : directions.entrySet()) {
            String direction = entry.getKey();
            Coordinate offset = entry.getValue();
            Coordinate neighborCoord = new Coordinate(x + offset.x, y + offset.y);

            // Check if neighboring room exists
            if (room_info.containsKey(neighborCoord)) {
                connectionDirections.append(direction); // Add direction to the list

                // If the neighboring room is updated, append the opposite direction
                String oppositeDirection = getOppositeDirection(direction);
                String existingConnections = room_info.get(neighborCoord);

                String neighbourCordUpdate = existingConnections + oppositeDirection;

                room_info_unsorted.put(neighborCoord, room_info.get(neighborCoord).split("_")[0] + "_" + neighbourCordUpdate);

                String sortedNeighbourCord = sortStringInOrder(neighbourCordUpdate);

                room_info.put(neighborCoord, room_info.get(neighborCoord).split("_")[0] + "_" + sortedNeighbourCord);

            }
        }

        connections.append(sortStringInOrder(connectionDirections.toString()));

        // Add the room to the room_info HashMap with the connection string
        room_info.put(coord, connections.toString());
    }

    // Method to sort a string based on a specific order
    private String sortStringInOrder(String input) {
        StringBuilder newString = new StringBuilder();

        if(input.contains("N")) {
            newString.append("N");
        }
        if(input.contains("S")) {
            newString.append("S");
        }
        if(input.contains("E")) {
            newString.append("E");
        }
        if(input.contains("W")) {
            newString.append("W");
        }

        return new String(newString);
    }

    public void place_boss_room() {
        if (current_boss_room > 0) { // Check before placing
            List<String> available_directions = getAvailableDirections();

            if (available_directions != null) {
                String random_direction = available_directions.get(random.nextInt(available_directions.size()));
                Coordinate offset = directions.get(random_direction);

                int new_x = dungeon_current_x + offset.x;
                int new_y = dungeon_current_y + offset.y;

                add_room(new_x, new_y, "boss");
                current_boss_room -= 1;

//                // Check if the boss room can be placed without being too close to another boss room
//                if (canPlaceBossRoom(new_x, new_y)) {
//                     // Decrement the count of remaining boss rooms
//                    Bukkit.getLogger().info("Boss room placed at: (" + new_x + ", " + new_y + ")");
//                }
            }
        }
    }

    // Method to check if a boss room can be placed based on existing rooms
    private boolean canPlaceBossRoom(int x, int y) {
        // Check all neighboring positions
        for (Coordinate direction : directions.values()) {
            Coordinate neighborCoord = new Coordinate(x + direction.x, y + direction.y);
            if (room_info.containsKey(neighborCoord) && room_info.get(neighborCoord).startsWith("boss")) {
                return false; // A boss room is too close
            }
        }
        return true; // Placement is valid
    }


    private void resetDungeon() {
        dungeon_current_x = 0;
        dungeon_current_y = 0;
        dungeon_current_room = 0;
        room_info.clear();
        add_room(dungeon_current_x, dungeon_current_y, "start");
    }

    // Asynchronous dungeon generation
    public void generateDungeonAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    generateDungeon();
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error occurred during dungeon generation: " + e.getMessage());
                    if (currentRetries < maxRetries) {
                        Bukkit.getLogger().info("Retrying dungeon generation... Attempt " + (currentRetries + 1));
                        currentRetries++;
                        generateDungeonAsync();
                    } else {
                        Bukkit.getLogger().severe("Max retry limit reached. Dungeon generation aborted.");
                    }
                }
            }
        }.runTaskAsynchronously(dungeonGeneratorPractice);  // Runs the task asynchronously
    }

    public void generateDungeon() {
        dungeon_current_x = 0;
        dungeon_current_y = 0;
        dungeon_current_room = 0;
        int max_restart = 4;

        // Start at 0,0 coord
        add_room(dungeon_current_x, dungeon_current_y, "start");

        // Main dungeon generation loop
        while (dungeon_current_room < total_dungeon_room) {
            try {
                // Check if restart is required
                if (restart_check() && max_restart > 0) {
                    max_restart -= 1;
                    resetDungeon();
                    continue;
                }

                // Get available directions
                List<String> available_directions = getAvailableDirections();
                if (available_directions == null || available_directions.isEmpty()) {
                    max_restart -= 1;
                    if (max_restart <= 0) {
                        return; // End dungeon generation if no restarts are left
                    }
                    resetDungeon();
                    continue;
                }

                // Pick a random direction and calculate new coordinates
                String random_direction = available_directions.get(random.nextInt(available_directions.size()));
                Coordinate offset = directions.get(random_direction);
                int new_x = dungeon_current_x + offset.x;
                int new_y = dungeon_current_y + offset.y;

                // Add the new room
                add_room(new_x, new_y, "room");

                // Update current coordinates
                dungeon_current_x = new_x;
                dungeon_current_y = new_y;

                // Place boss room if conditions are met
//                if (current_boss_room > 0) {
//                    // Adjusted conditions for boss room placement
//                    if ((total_boss_room == 2 && dungeon_current_room >= (total_dungeon_room * 80) / 100) ||
//                            (total_boss_room == 1 && dungeon_current_room >= (total_dungeon_room * 99) / 100)) {
//                        place_boss_room();
//                    }
//                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error occurred during dungeon generation: " + e.getMessage());
                e.printStackTrace();

                // If an error occurs, try restarting the generation process
                max_restart -= 1;
                if (max_restart > 0) {
                    Bukkit.getLogger().info("Restarting dungeon generation...");
                    resetDungeon();
                } else {
                    Bukkit.getLogger().severe("Dungeon generation failed after multiple attempts.");
                    return; // End dungeon generation after max restarts
                }
            }
        }

        // Print dungeon info and optionally display the dungeon map

        boolean displayMap = config.getBoolean("display-dungeon-map");
        boolean displayInfo = config.getBoolean("display-dungeon-info");
        if(displayInfo) {
            tellInfo();
        } else {
            Bukkit.getLogger().info("A dungeon was created. Turn on display-info in config for details.");
        }

        // Place Dungeon Rooms
        DatabaseFetcher db = new DatabaseFetcher(dungeonGeneratorPractice);
        try {
            db.connect(); // Establish the connection before fetching schematics
            DungeonRoomPicker roomPicker = new DungeonRoomPicker(dungeonGeneratorPractice, room_info, db, worldName);
            // Proceed with other operations...
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to connect to the database: " + e.getMessage());
        }

        if (displayMap) {
            printDungeonMap();
        } else {
            Bukkit.getLogger().info("Dungeon map is not being printed in console.");
        }

        Bukkit.getLogger().info("Dungeon generation complete!");
    }

    ////////////////////////////
    public void printDungeonMap() {
        if (room_info.isEmpty()) { // Check if any rooms were placed
            System.out.println("No rooms placed in the dungeon.");
            return;
        }

        // Find min and max coordinates
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Coordinate coord : room_info.keySet()) {
            if (coord.x < minX) minX = coord.x;
            if (coord.x > maxX) maxX = coord.x;
            if (coord.y < minY) minY = coord.y;
            if (coord.y > maxY) maxY = coord.y;
        }

        // Print the dungeon layout
        for (int y = maxY; y >= minY; y--) {
            StringBuilder row = new StringBuilder();
            for (int x = minX; x <= maxX; x++) {
                Coordinate currentCoord = new Coordinate(x, y);

                if (room_info.containsKey(currentCoord)) {
                    String room = room_info.get(currentCoord);
                    String connections = getRoomConnections(currentCoord); // Assume this method returns connection directions as a string

                    char roomType;
                    if (room.startsWith("start")) {
                        roomType = 'S'; // Starting room
                    } else if (room.startsWith("boss")) {
                        roomType = 'B'; // Boss room
                        connections = connections.substring(0, 1); // Only show one connection for isolated rooms
                    } else if (room.startsWith("isolated")) {
                        roomType = 'I'; // Isolated room
                    } else {
                        roomType = 'R'; // Normal room
                    }

                    row.append("(").append(roomType).append(":").append(connections).append(") ");
                } else {
                    row.append("(----) "); // Empty space
                }
            }
            System.out.println(row.toString().trim());
        }
    }

    private String getRoomConnections(Coordinate coord) {
        // Extract connection information for the room at the given coordinate
        // You may need to adapt this part to match your existing room structure
        String connections = ""; // Initialize an empty string for connections

        // Example logic to determine connections; adapt as needed
        // You may need to check the adjacent rooms to determine the connections
        if (room_info.containsKey(new Coordinate(coord.x, coord.y + 1))) connections += "N"; // North
        if (room_info.containsKey(new Coordinate(coord.x, coord.y - 1))) connections += "S"; // South
        if (room_info.containsKey(new Coordinate(coord.x + 1, coord.y))) connections += "E"; // East
        if (room_info.containsKey(new Coordinate(coord.x - 1, coord.y))) connections += "W"; // West

        return connections;
    }


}

