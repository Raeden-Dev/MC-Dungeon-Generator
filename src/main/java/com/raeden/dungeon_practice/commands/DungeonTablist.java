package com.raeden.dungeon_practice.commands;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DungeonTablist implements TabCompleter {

    private final DungeonGeneratorPractice dungeonPractice;

    private Set<String> dungeonTypes;

    public DungeonTablist(DungeonGeneratorPractice dungeonPractice) {
        this.dungeonPractice = dungeonPractice;
    }
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

        List<String> args_results = new ArrayList<>();

        // Get dungeon types from the configuration
        FileConfiguration config = dungeonPractice.getConfig();
        if (config.getConfigurationSection("dungeon-types") != null) {
            dungeonTypes = config.getConfigurationSection("dungeon-types").getKeys(false);
            if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
                args_results.addAll(dungeonTypes);
            }
        } else {
            Bukkit.getLogger().warning("Dungeon types are not configured in the config file!");
            Bukkit.getLogger().warning("Dungeon Types: " +
                    dungeonTypes);
        }


        if(args.length == 1) {
            args_results.add("create");
            args_results.add("teleport");
            args_results.add("delete");
            args_results.add("tpov");
            args_results.add("db-check");
            args_results.add("schematic");
            args_results.add("config");
            args_results.add("info");
        }

        if(args.length == 2) {
            if (args[0].equalsIgnoreCase("schematic") || args[0].equalsIgnoreCase("sch")) {
                args_results.add("check");
                args_results.add("reload");
            }

            else if(args[0].equalsIgnoreCase("create")) {
                args_results.addAll(dungeonTypes);
            }
            else if(args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("dlt")) {
                args_results.addAll(DungeonGeneratorPractice.generated_dungeon_count);
                args_results.add("all");
            }
            else if(args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
                args_results.addAll(DungeonGeneratorPractice.generated_dungeon_count);
            }
            else if(args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("cfg")) {
                args_results.add("reload");
            }
        }


        return args_results;
    }
}
