package io.github.finnperera.playmodular.initialframework;

import java.util.*;

public class HiveRuleEngine {
    public static final int NUM_NEIGHBOURS = 6;

    public List<HiveMove> generatePieceMoves(HiveBoardState boardState, HiveTile hiveTile) {
        List<HiveMove> moves = new ArrayList<HiveMove>();
        switch (hiveTile.getTileType()) {
            case QUEEN_BEE -> moves = queenMoves(boardState, hiveTile);
        }
        return null;
    }

    private List<HiveMove> queenMoves(HiveBoardState boardState, HiveTile hiveTile) {
        // Tile must belong to board
        assert boardState.getBoard().get(hiveTile.getHex()).equals(hiveTile);

        List<HiveMove> moves = new ArrayList<>();
        for (Hex neighbouringTile : Hex.getNeighbours(hiveTile.getHex())) {
            // tile empty check
            if (boardState.getBoard().containsKey(neighbouringTile)) continue;

            // free to move
            if (!isFreeToMove(boardState, hiveTile, neighbouringTile)) continue;

            // remains one hive
            if (!isOneHiveWhileMoving(boardState, hiveTile)) continue;

            // add move to list
            moves.add(new HiveMove(hiveTile, neighbouringTile));
        }
        return moves;
    }

    public Move generatePlacementMoves() {
        return null;
    }

    public boolean isFreeToMove(HiveBoardState boardState, HiveTile tileToMove, Hex nextPosition) {
        // Move must belong to board
        assert boardState.getBoard().get(tileToMove.getHex()).equals(tileToMove);
        Hex direction = Hex.hexSubtract(tileToMove.getHex(), nextPosition);
        int directionIndex = Hex.hexDirectionAsIndex(direction);

        boolean leftDirectionClear = boardState.getBoard().containsKey(
                Hex.hexNeighbour(tileToMove.getHex(), (directionIndex + 1) % 6));
        boolean rightDirectionClear = boardState.getBoard().containsKey(
                Hex.hexNeighbour(tileToMove.getHex(), (directionIndex - 1) % 6));

        return leftDirectionClear || rightDirectionClear;
    }

    // alternative, might be better if using direction index instead of full move?
    public boolean isFreeToMove(HiveBoardState boardState, HiveTile hiveTile, int directionIndex) {
        // Move must belong to board
        assert boardState.getBoard().get(hiveTile.getHex()).equals(hiveTile);

        boolean leftDirectionClear = boardState.getBoard().containsKey(
                Hex.hexNeighbour(hiveTile.getHex(), (directionIndex + 1) % 6));
        boolean rightDirectionClear = boardState.getBoard().containsKey(
                Hex.hexNeighbour(hiveTile.getHex(), (directionIndex - 1) % 6));

        return leftDirectionClear || rightDirectionClear;
    }

    // BFS and check if all states visited, allows for skipping a tile (for oneHiveMoving)
    public boolean isConnected(HashMap<Hex, HiveTile> board, Hex startingTile, Hex tileToSkip) {
        if (startingTile == null) return true;

        HashSet<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new LinkedList<>();
        queue.offer(startingTile);
        visited.add(tileToSkip);

        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            visited.add(current);

            for (Hex neighbour : Hex.getNeighbours(current)) {
                if (neighbour.equals(tileToSkip)) continue;

                if (board.containsKey(neighbour) && !visited.contains(neighbour)) {
                    queue.offer(neighbour);
                }
            }
        }

        return visited.containsAll(board.keySet());
    }

    public boolean isOneHive(HiveBoardState boardState) {
        HashMap<Hex, HiveTile> board = boardState.getBoard();
        Hex tile = board.isEmpty() ? null : board.entrySet().iterator().next().getKey();
        return isConnected(board, tile, null);
    }

    public boolean isOneHiveWhileMoving(HiveBoardState boardState, HiveTile hiveTile) {
        HashMap<Hex, HiveTile> board = boardState.getBoard();
        Hex tile = board.isEmpty() ? null : board.entrySet().iterator().next().getKey();
        return isConnected(board, tile, hiveTile.getHex());
    }
}
