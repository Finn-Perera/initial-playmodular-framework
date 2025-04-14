package io.github.finnperera.playmodular.initialframework.HivePlayers;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.HashMap;
import java.util.List;

public class HivePlayer implements Player, ConfigurableOptions {

    private final HiveColour colour;
    private String playerID = getClass().getSimpleName();
    private HashMap<HiveTileType, Integer> tiles;

    public HivePlayer(HiveColour colour) {
        tiles = new HashMap<>();
        createHand();

        this.colour = colour;
        playerID = getClass().getSimpleName();
    }

    public HivePlayer(HashMap<HiveTileType, Integer> tiles, HiveColour colour, String playerID) {
        this.tiles = new HashMap<>(tiles);
        this.colour = colour;
        this.playerID = playerID;
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
    public String getPlayerID() {
        return playerID;
    }

    @Override
    public boolean isAI() {
        return false;
    }

    @Override
    public List<Option<?>> getOptions() {
        return List.of(
                new Option<>("Player ID", "Identifier for Player", OptionType.TEXTBOX, String.class, getClass().getSimpleName(), null, null)
        );
    }

    @Override
    public void setOptions(List<Option<?>> options) {
        for (Option<?> option : options) {
            if (option.getName().equals("Player ID")) {
                playerID = (String) option.getValue();
            } else {
                throw new IllegalArgumentException("Unknown option: " + option.getName());
            }
        }
    }
}
