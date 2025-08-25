package com.raeden.dungeon_practice.generators;

import com.raeden.dungeon_practice.DungeonGeneratorPractice;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DungeonSchematicPaster {

    private final DungeonGeneratorPractice dungeonGeneratorPractice;

    private final int offset = 8;
    private int delaySeconds;  // Delay in seconds before pasting the next schematic

    public DungeonSchematicPaster(DungeonGeneratorPractice dungeonGeneratorPractice, int delaySeconds) {
        this.dungeonGeneratorPractice = dungeonGeneratorPractice;
        this.delaySeconds = delaySeconds;  // Configurable delay
    }

    public void pasteSchematic(String schematicName, String schematicLocation, String worldName, int gridX, int gridY, int gridZ) {
        File schematicFile = new File(schematicLocation, schematicName + ".schem");
        if (!schematicFile.exists()) {
            Bukkit.getLogger().severe("While pasting schematic, file was not found: " + schematicFile.getPath());
            return;
        }

        // Get the world to paste the schematic into
        BukkitWorld bukkitWorld = new BukkitWorld(Bukkit.getWorld(worldName));
        if (bukkitWorld == null) {
            Bukkit.getLogger().severe("World was not found: " + worldName);
            return;
        }

        // Load the schematic into a Clipboard asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                Clipboard clipboard;
                try {
                    ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                    try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                        clipboard = reader.read();
                    }

                    // Paste the schematic with delay
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try (EditSession editSession = WorldEdit.getInstance().newEditSession(bukkitWorld)) {
                                Operation operation = new ClipboardHolder(clipboard)
                                        .createPaste(editSession)
                                        .to(BlockVector3.at(gridX - offset, gridY, gridZ - offset))
                                        .build();
                                Operations.complete(operation);

                                //Bukkit.getLogger().info("Schematic " + schematicName + " pasted successfully at (" + gridX + ", " + gridY + ", " + gridZ + ") in world " + worldName);
                            } catch (WorldEditException e) {
                                Bukkit.getLogger().severe("Failed to paste schematic: " + e.getMessage());
                            }
                        }
                    }.runTaskLater(dungeonGeneratorPractice, delaySeconds * 20L);  // Schedule with delay (20 ticks per second)
                } catch (IOException e) {
                    Bukkit.getLogger().severe("Failed to load schematic: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(dungeonGeneratorPractice);
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;  // Allow changing delay time dynamically
    }
}
