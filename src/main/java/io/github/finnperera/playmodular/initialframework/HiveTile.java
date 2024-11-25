package io.github.finnperera.playmodular.initialframework;

import java.util.Objects;

public class HiveTile implements Piece {

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
}
