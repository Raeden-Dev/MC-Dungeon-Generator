package com.raeden.dungeon_practice.manager;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import com.raeden.dungeon_practice.database.DatabaseFetcher;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

/*
* This class currently handles the following operations:
1. Load Schematics from config/plugin/dungeons folder (loadSchematics)
    - Children:
    1.1: loadSchematicsFromTheme (Goes through theme folders present in dungeon folder and loads each schematic)
        1.1.1: extractInfoFromName (Extracts various information of schematic from its name)
        * PASSED ON TO DATABASE CLASS:
            * checkSchematicExists (CHECK IF THE SCHEMATIC EXISTS)
            * insertSchematic (INSERTS SCHEMATIC INTO THE DATABASE)
*/


public class SchematicManager {
    private final File dungeonSchematicFolder;
    private final Logger logger;
    private final DatabaseFetcher database;

    private final DungeonGeneratorPractice dungeonPractice;

    public SchematicManager(DungeonGeneratorPractice dungeonPractice, File dungeonSchematicFolder, Logger logger, DatabaseFetcher database) {
        this.dungeonPractice = dungeonPractice;
        this.dungeonSchematicFolder = dungeonSchematicFolder;
        this.logger = logger;
        this.database = database;

        loadSchematics();
    }

    // Load schematics from folder
    public void loadSchematics() {
        if (!dungeonSchematicFolder.exists() || !dungeonSchematicFolder.isDirectory()) {
            logger.warning("Dungeon base folder not found: " + dungeonSchematicFolder.getAbsolutePath());
            return;
        }
        for (File themeFolder : Objects.requireNonNull(dungeonSchematicFolder.listFiles())) {
            if (themeFolder.isDirectory()) {
                loadSchematicsFromTheme(themeFolder);
            }
        }
    }

    // Load Theme Schematics
    private void loadSchematicsFromTheme(File themeFolder) {
        for (File schematicFile : Objects.requireNonNull(themeFolder.listFiles())) {
            if (schematicFile.isFile() && (schematicFile.getName().endsWith(".schem") || schematicFile.getName().endsWith(".schematic"))) {
                String schematicName = schematicFile.getName();
                // Extract schematic Info
                SchematicInfo info = extractInfoFromName(schematicName);
                if (info != null) {
                    try {
                        // Check if the schematic already exists in the database
                        if (!database.checkSchematicExists(info)) {
                            // If not present, insert schematic into the database
                            database.insertSchematic(info, themeFolder.getPath());
                            logger.info("Loaded schematic into database: " + schematicName +
                                    " (Type: " + info.getDungeonType() +
                                    ", Direction: " + info.getDirection() +
                                    ", Name: " + info.getDungeonName() +
                                    ", Variation Number: " + info.getVariationNumber() + ")");
                        } else {
                            // Log that the schematic already exists
                            FileConfiguration config = dungeonPractice.getConfig();
                            boolean display_check = config.getBoolean("display-loaded-schematics");
                            if (display_check) {
                                logger.info("Schematic already exists in the database: " + schematicName);
                            }
                        }
                    } catch (SQLException e) {
                        logger.severe("Failed to save schematic to database: " + e.getMessage());
                    }
                }
            }
        }
    }


    private SchematicInfo extractInfoFromName(String schematicName) {
        String[] parts;

        // Remove extension and split
        if (schematicName.endsWith(".schematic")) {
            parts = schematicName.replace(".schematic", "").split("_");
        } else if (schematicName.endsWith(".schem")) {
            parts = schematicName.replace(".schem", "").split("_");
        } else {
            return null; // Return null if the file does not have the expected extension
        }

        // A schematic should always have four parts that is:
        // [dungeon_type]_[dungeon_direction]_[dungeon_variation_name]_[variation_number]
        if (parts.length < 4) {
            return null;
        }

        String dungeonType = parts[0];
        String dungeonDirection = parts[1];
        String dungeonName = String.join("_", Arrays.copyOfRange(parts, 2, parts.length - 1));
        String variationNumberStr = parts[parts.length - 1];

        int variationNumber;
        try {
            variationNumber = Integer.parseInt(variationNumberStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }

        return new SchematicInfo(dungeonType, dungeonDirection, dungeonName, String.valueOf(variationNumber));
    }
}
