package io.github.finnperera.playmodular.initialframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class HiveBoardState implements BoardState<Hex, HiveTile> {
    private MapBasedStorage<Hex, Stack<HiveTile>> board;

    public HiveBoardState() {
        this.board = new MapBasedStorage<>();
    }

    public HiveBoardState(MapBasedStorage<Hex, Stack<HiveTile>> board) {
        this.board = new MapBasedStorage<>(board);
    }

    public HiveBoardState(HiveBoardState boardState) {
        this.board = new MapBasedStorage<>(boardState.board);
    }

    public boolean hasTileAtHex(Hex hex) {
        return board.hasPieceAt(hex);
    }

    @Override
    public HiveTile getPieceAt(Hex position) {
        return board.hasPieceAt(position) ? board.getPieceAt(position).peek() : null;
    }

    // Should be immutable
    @Override
    public void placePiece(Hex position, HiveTile piece) {
        if (board.hasPieceAt(position)) {
            board.getPieceAt(position).push(piece);
        } else {
            Stack<HiveTile> stack = new Stack<>();
            stack.push(piece);
            board.placePieceAt(position, stack);
        }
    }

    // Should be immutable
    @Override
    public void removePieceAt(Hex position) {
        assert !board.getPieceAt(position).isEmpty();
        board.getPieceAt(position).pop();
        if (board.getPieceAt(position).isEmpty()) {
            board.removePieceAt(position);
        }
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
        return board.getPieceAt(positions.get(rand.nextInt(positions.size()))).peek();
    }

    // could be more efficient?
    public List<HiveTile> getAllPiecesOfPlayer(HivePlayer player) {
        List<HiveTile> pieces = new ArrayList<>();
        List<Hex> positions = getAllPositions();
        HiveColour playerColour = player.getColour();
        for (Hex hex : positions) {
            if (playerColour == getPieceAt(hex).getColour()) {
                pieces.add(getPieceAt(hex));
            }
        }
        return pieces;
    }

    public List<HiveTile> getQueens() {
        List<HiveTile> queens = new ArrayList<>();
        for (Stack<HiveTile> stack : board.getAllPieces()) {
            for (HiveTile piece : stack) {
                if (piece.getTileType() == HiveTileType.QUEEN_BEE) queens.add(piece);
            }
        }
        return queens;
    }

    // REMOVE AFTER TESTING
    public MapBasedStorage<Hex, Stack<HiveTile>> getBoard() {
        return board;
    }
}
