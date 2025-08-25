package com.raeden.dungeon_practice.manager;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/*
* This class currently handles the following operations:
1.
*/

public class ConfigManager {
    private static FileConfiguration config;

    public static void setupConfig(DungeonGeneratorPractice dungeon_practice) {
        ConfigManager.config = dungeon_practice.getConfig();
        dungeon_practice.saveDefaultConfig();
    }

    public static List<String> getDungeonDifficulties() {
        return new ArrayList<>(config.getConfigurationSection("dungeon-difficulties").getKeys(false));
    }

}
