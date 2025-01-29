package io.github.finnperera.playmodular.initialframework.HivePlayers;

import io.github.finnperera.playmodular.initialframework.HiveColour;
import io.github.finnperera.playmodular.initialframework.HiveTileType;
import io.github.finnperera.playmodular.initialframework.Player;

import java.util.HashMap;

public class HivePlayer implements Player {

    private HashMap<HiveTileType, Integer> tiles;
    private final HiveColour colour;

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

    public HashMap<HiveTileType, Integer> removeTile(HiveTileType type) {
        assert tiles.get(type) != null && tiles.get(type) > 0;
        HashMap<HiveTileType, Integer> result = new HashMap<>(tiles);
        result.put(type, result.get(type) - 1);
        return result;
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

    @Override
    public boolean isAI() {
        return false;
    }
}
