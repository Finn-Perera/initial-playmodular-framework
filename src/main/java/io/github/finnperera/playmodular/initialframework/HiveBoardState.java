package io.github.finnperera.playmodular.initialframework;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class HiveBoardState<Hex, HiveTile> implements BoardState<Hex, HiveTile> {
    private MapBasedStorage<Hex, HiveTile> board = new MapBasedStorage<>();

    private HivePlayer player1;
    private HivePlayer player2;

    public HiveBoardState(HivePlayer player1, HivePlayer player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public boolean hasTileAtHex(Hex hex) {
        return board.hasPieceAt(hex);
    }

    public HivePlayer getPlayer1() {
        return player1;
    }

    public HivePlayer getPlayer2() {
        return player2;
    }

    @Override
    public HiveTile getPieceAt(Hex position) {
        return board.getPieceAt(position);
    }

    @Override
    public void placePiece(Hex position, HiveTile piece) {
        board.placePieceAt(position, piece);
    }

    @Override
    public void removePieceAt(Hex position) {
        board.removePieceAt(position);
    }

    @Override
    public List<Hex> getAllPositions() {
        return board.getAllPositions();
    }

    @Override
    public boolean hasPieceAt(Hex position) {
        return board.hasPieceAt(position);
    }

    @Override
    public boolean isBoardEmpty() {
        return board.isEmpty();
    }

    @Override
    public int getPieceCount() {
        return board.size();
    }

    @Override
    public HiveTile getRandomPiece() {
        List<Hex> positions = getAllPositions();
        if (positions.isEmpty()) {return null;}
        Random rand = new Random();
        return board.getPieceAt(positions.get(rand.nextInt(positions.size())));
    }
}
