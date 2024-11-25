package io.github.finnperera.playmodular.initialframework;

import java.util.HashMap;

public class HiveBoardState implements BoardState {

    private HashMap<Hex, HiveTile> board = new HashMap<>();

    private HivePlayer player1;
    private HivePlayer player2;

    public HiveBoardState(HivePlayer player1, HivePlayer player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public HashMap<Hex, HiveTile> getBoard() {
        return board;
    }

    public boolean hasTileAtHex(Hex hex) {
        return board.containsKey(hex);
    }

    public HivePlayer getPlayer1() {
        return player1;
    }

    public HivePlayer getPlayer2() {
        return player2;
    }
}
