package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class HiveBoardState implements BoardState<Hex, HiveTile> {
    private MapBasedStorage<Hex, Stack<HiveTile>> board;

    public HiveBoardState() {
        this.board = new MapBasedStorage<>();
    }

    public HiveBoardState(HiveBoardState boardState) {
        this.board = new MapBasedStorage<>(boardState.board, stack -> {
            Stack<HiveTile> newStack = new Stack<>();
            for (HiveTile tile : stack) {
                newStack.push(new HiveTile(tile.getTileType(), tile.getHex(), tile.getColour()));
            }
            return newStack;
        });
    }

    public boolean hasTileAtHex(Hex hex) {
        return board.hasPieceAt(hex);
    }

    @Override
    public HiveTile getPieceAt(Hex position) {
        if (!board.hasPieceAt(position)) return null;
        Stack<HiveTile> stack = board.getPieceAt(position);
        return stack.isEmpty() ? null : stack.peek();
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
        Stack<HiveTile> stack = board.getPieceAt(position);
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("Trying to remove from an empty position" + position);
        }

        stack.pop();
        if (stack.isEmpty()) {
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
        List<Stack<HiveTile>> pieces = board.getAllPieces();
        int numPieces = 0;
        for (Stack<HiveTile> piece : pieces) {
            numPieces += piece.size();
        }
        return numPieces;
    }

    @Override
    public HiveTile getRandomPiece() {
        List<Hex> positions = getAllPositions();
        if (positions.isEmpty()) {
            return null;
        }

        Random rand = new Random();

        for (int i = 0; i < positions.size(); i++) {
            Hex randomPos = positions.get(rand.nextInt(positions.size()));
            Stack<HiveTile> stack = board.getPieceAt(randomPos);

            if (!stack.isEmpty()) {
                return stack.peek();
            }
        }
        return null;
    }

    public List<HiveTile> getAllPiecesOfPlayer(HivePlayer player) {
        List<Stack<HiveTile>> allPositions = board.getAllPieces();
        List<HiveTile> pieces = new ArrayList<>();
        for (Stack<HiveTile> position : allPositions) {
            for (HiveTile tile : position) {
                if (player.getColour().equals(tile.getColour())) {
                    pieces.add(tile);
                }
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

    public HiveTile getQueenOfPlayer(HivePlayer player) {
        for (Stack<HiveTile> stack : board.getAllPieces()) {
            for (HiveTile piece : stack) {
                if (piece.getTileType() == HiveTileType.QUEEN_BEE && piece.getColour().equals(player.getColour())) {
                    return piece;
                }
            }
        }
        return null;
    }

    // REMOVE AFTER TESTING
    public MapBasedStorage<Hex, Stack<HiveTile>> getBoard() {
        return board;
    }
}
