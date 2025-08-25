package com.raeden.dungeon_practice.manager;

public class RPSchematicInfo {
    private final String name;
    private final String filePath;

    public RPSchematicInfo(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }
}