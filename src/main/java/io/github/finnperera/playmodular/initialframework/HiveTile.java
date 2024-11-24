package io.github.finnperera.playmodular.initialframework;

public class HiveTile implements Piece {

    private HiveTileType tileType;

    private Hex hex;

    public HiveTile(HiveTileType tileType, Hex hex) {
        this.tileType = tileType;
        this.hex = hex;
    }

    public HiveTileType getTileType() {
        return tileType;
    }

    public Hex getHex() {
        return hex;
    }
}
