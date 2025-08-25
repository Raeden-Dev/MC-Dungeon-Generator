package com.raeden.dungeon_practice.database;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import com.raeden.dungeon_practice.generators.DungeonRoomPicker;
import com.raeden.dungeon_practice.manager.RPSchematicInfo;
import com.raeden.dungeon_practice.manager.SchematicInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
* This class currently handles the following operations:
1. Load database info from config to get schematics (loadDatabaseInfo)
    - Get Name of database to which it's connected with (getDatabaseName)
2. Establish a connection to database (connect)
    - Check connection status (isConnected)
3. Disconnect from a database (disconnect)
4. Insert schematic into database directly from taken info (insertSchematic)
5. Check if a certain schematic exists (checkSchematicExists)
6. Get total schematic count (getSchematicCount)
*/

public class DatabaseFetcher {
    private final DungeonGeneratorPractice plugin;
    private Connection connection;

    private String HOST;
    private int PORT;
    private String DATABASE;
    private String USERNAME;
    private String PASSWORD;

    public DatabaseFetcher(DungeonGeneratorPractice plugin) {
        this.plugin = plugin;
        loadDatabaseInfo();
    }

    // Load database info directly from config
    private void loadDatabaseInfo() {
        FileConfiguration config = plugin.getConfig();

        this.HOST = config.getString("database-info.HOST");
        this.PORT = config.getInt("database-info.PORT");
        this.DATABASE = config.getString("database-info.DATABASE");
        this.USERNAME = config.getString("database-info.USERNAME");
        this.PASSWORD = config.getString("database-info.PASSWORD");

        // Handle "no-pass" case
        if (PASSWORD.equals("no-pass")) {
            this.PASSWORD = "";
        }

        // Validate that all required info is present
        if (HOST == null || DATABASE == null || USERNAME == null || PORT == 0) {
            throw new IllegalArgumentException("One or more required database configuration values are missing.");
        }
    }

    public String getDatabaseName() { return DATABASE; }

    // Establish connection
    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false",
                    USERNAME, PASSWORD);
            plugin.getLogger().info("Connected to the database.");
        }
    }

    // Check if connected
    public boolean isConnected() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    // Disconnect from database
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Disconnected from the database.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error closing the database connection: " + e.getMessage());
            }
        }
    }



    // Insert the schematic info into Database (This info is taken from SchematicManager class)
    public void insertSchematic(SchematicInfo info, String filePath) throws SQLException {
        String query = "INSERT INTO schematics (dungeon_type, dungeon_direction, dungeon_name, variation_number, file_path) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, info.getDungeonType());
            statement.setString(2, info.getDirection());
            statement.setString(3, info.getDungeonName());

            statement.setInt(4, Integer.parseInt(info.getVariationNumber()));
            statement.setString(5, filePath);
            statement.executeUpdate();
        }
    }
    // NCheck if a schematic exists in the database
    public boolean checkSchematicExists(SchematicInfo info) throws SQLException {
        String query = "SELECT COUNT(*) FROM schematics WHERE dungeon_type = ? AND dungeon_direction = ? AND dungeon_name = ? AND variation_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, info.getDungeonType());
            statement.setString(2, info.getDirection());
            statement.setString(3, info.getDungeonName());
            statement.setString(4, info.getVariationNumber());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // Return true if count > 0
                }
            }
        }
        return false;
    }

    // Method to get total count of schematics in the database
    public int getSchematicCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM schematics";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1); // Return the count from the first column
            }
        }
        return 0; // Return 0 if no results
    }

    public List<RPSchematicInfo> fetchSchematicsByDirection(String direction) {
        List<RPSchematicInfo> schematics = new ArrayList<>();
        String query = "SELECT dungeon_type, dungeon_name, variation_number, file_path FROM schematics WHERE dungeon_direction = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, direction);
            //Bukkit.getLogger().info("Executing query: " + query + " with direction: " + direction);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String dungeonType = resultSet.getString("dungeon_type");
                String dungeonName = resultSet.getString("dungeon_name");
                int variationNumber = resultSet.getInt("variation_number");
                String filePath = resultSet.getString("file_path");

                // Check if the room type requires specific keywords
                if ((direction.equals("start") && !dungeonName.toLowerCase().contains("start")) ||
                        (direction.equals("boss") && !dungeonName.toLowerCase().contains("boss"))) {
                    continue; // Skip if it doesn't match the keyword requirements
                }

                // Construct schematic name
                String schematicName = String.format("%s_%s_%s_%d", dungeonType, direction, dungeonName, variationNumber);
                schematics.add(new RPSchematicInfo(schematicName, filePath));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Database error: " + e.getMessage());
        }

        // Log how many schematics were found
        //Bukkit.getLogger().info("Found " + schematics.size() + " schematics for direction: " + direction);

        return schematics;
    }



}
