package io.github.finnperera.playmodular.initialframework;

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
}
