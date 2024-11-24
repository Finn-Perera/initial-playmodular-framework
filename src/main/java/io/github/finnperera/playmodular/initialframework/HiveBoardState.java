package io.github.finnperera.playmodular.initialframework;

import java.util.HashMap;

public class HiveBoardState implements BoardState {

    private HashMap<Hex, HiveTile> board = new HashMap<>();
    private HashMap<HiveTileType, Integer> player1Tiles = new HashMap<>();
    private HashMap<HiveTileType, Integer> player2Tiles = new HashMap<>();

    public void fillHand(HashMap<HiveTileType, Integer> playerHand) {
        playerHand.put(HiveTileType.QUEEN_BEE, 1);
        playerHand.put(HiveTileType.GRASSHOPPER, 2);
        playerHand.put(HiveTileType.BEETLE, 2);
        playerHand.put(HiveTileType.SOLDIER_ANT, 3);
        playerHand.put(HiveTileType.SPIDER, 3);
    }

    public HashMap<Hex, HiveTile> getBoard() {
        return board;
    }
}
