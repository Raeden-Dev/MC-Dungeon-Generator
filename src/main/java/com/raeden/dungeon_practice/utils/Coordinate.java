package com.raeden.dungeon_practice.utils;

import java.util.Objects;

public class Coordinate {
    public int x;  // X coordinate
    public int y;  // Y coordinate

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinate)) return false;
        Coordinate other = (Coordinate) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public int getX() { return x;}

    public int getY() { return y;}
}
