package com.raeden.dungeon_practice.manager;

// SchematicInfo class to store info about schematic:
public class SchematicInfo {
    private final String dungeonType;
    private final String dungeonDirection;
    private final String dungeonVariationNum;
    private final String dungeonName;

    public SchematicInfo(String dungeonType,String dungeonDirection,String dungeonName,String dungeonVariationNum) {
        this.dungeonType = dungeonType;
        this.dungeonDirection = dungeonDirection;
        this.dungeonVariationNum = dungeonVariationNum;
        this.dungeonName = dungeonName;
    }
    public String getDungeonType() {return dungeonType;}
    public String getDirection() {return dungeonDirection;}
    public String getDungeonName() {return dungeonName;}
    public String getVariationNumber() {return dungeonVariationNum;}
}