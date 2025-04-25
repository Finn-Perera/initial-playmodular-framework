package io.github.finnperera.playmodular.initialframework;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HiveTile implements Piece, LoggableComponent {

    private HiveTileType tileType;
    private Hex hex;
    private HiveColour colour;

    public HiveTile(HiveTileType tileType, Hex hex, HiveColour colour) {
        this.tileType = tileType;
        this.hex = hex;
        this.colour = colour;
    }

    public HiveTileType getTileType() {
        return tileType;
    }

    public Hex getHex() {
        return hex;
    }

    public HiveColour getColour() {
        return colour;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HiveTile hiveTile = (HiveTile) o;
        return tileType == hiveTile.tileType && Objects.equals(hex, hiveTile.hex) && colour == hiveTile.colour;
    }

    @Override
    public String toString() {
        return "HiveTile{" +
                "tileType=" + tileType +
                ", hex=" + hex +
                ", colour=" + colour +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileType, hex, colour);
    }

    @Override
    public Map<String, Object> toLogMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("tile type", tileType.toString());
        map.put("colour", colour.toString());
        map.put("position", hex.toString());
        return map;
    }
}
