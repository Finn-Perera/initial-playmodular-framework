package io.github.finnperera.playmodular.initialframework;

import java.util.HashMap;

public class HivePlayer implements Player {

    private HashMap<HiveTileType, Integer> tiles;
    private HiveColour colour;

    public HivePlayer(HiveColour colour) {
        tiles = new HashMap<>();
        createHand();

        this.colour = colour;
    }

    public HivePlayer(HashMap<HiveTileType, Integer> tiles, HiveColour colour) {
        this.tiles = new HashMap<>(tiles);
        this.colour = colour;
    }

    private void createHand() {
        tiles.put(HiveTileType.QUEEN_BEE, 1);
        tiles.put(HiveTileType.GRASSHOPPER, 2);
        tiles.put(HiveTileType.BEETLE, 2);
        tiles.put(HiveTileType.ANT, 3);
        tiles.put(HiveTileType.SPIDER, 3);
    }

    public void removeTile(HiveTileType type) {
        assert tiles.get(type) != null && tiles.get(type) > 0;
        tiles.put(type, tiles.get(type) - 1);
    }

    public int getTypeRemainingTiles(HiveTileType type) {
        return tiles.getOrDefault(type, 0);
    }

    public void setHand(HashMap<HiveTileType, Integer> tiles) {
        this.tiles = tiles;
    }

    public HiveColour getColour() {
        return colour;
    }

    public HashMap<HiveTileType, Integer> getTiles() {
        return tiles;
    }
}
