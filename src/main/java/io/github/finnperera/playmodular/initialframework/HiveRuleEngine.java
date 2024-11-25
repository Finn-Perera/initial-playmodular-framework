package io.github.finnperera.playmodular.initialframework;

import java.util.*;

public class HiveRuleEngine {
    public static final int NUM_NEIGHBOURS = 6;

    public List<HiveMove> generatePieceMoves(HiveBoardState<Hex, HiveTile> boardState, HiveTile hiveTile) {
        List<HiveMove> moves = new ArrayList<HiveMove>();
        switch (hiveTile.getTileType()) {
            case QUEEN_BEE -> moves = queenMoves(boardState, hiveTile);
        }
        return moves;
    }

    private List<HiveMove> queenMoves(HiveBoardState<Hex, HiveTile> boardState, HiveTile hiveTile) {
        // Tile must belong to board
        assert boardState.getPieceAt(hiveTile.getHex()).equals(hiveTile);

        List<HiveMove> moves = new ArrayList<>();
        for (Hex neighbouringTile : hiveTile.getHex().getNeighbours()) {
            // tile empty check
            if (boardState.hasTileAtHex(neighbouringTile)) continue;

            // free to move
            if (!isFreeToMove(boardState, hiveTile, neighbouringTile)) continue;

            // remains one hive
            if (!isOneHiveWhileMoving(boardState, hiveTile)) continue;

            // add move to list
            moves.add(new HiveMove(hiveTile, neighbouringTile));
        }
        return moves;
    }

    public List<Hex> generatePlacementPositions(HiveBoardState<Hex, HiveTile> boardState, HivePlayer player) {
        assert player.equals(boardState.getPlayer1()) || player.equals(boardState.getPlayer2());
        HiveColour colour = player.getColour();

        // check if beginning board
        if (boardState.isBoardEmpty()) {
            return List.of(new Hex(0, 0, 0));
        } else if (boardState.getPieceCount() < 2) {
            return new Hex(0, 0, 0).getNeighbours(); // works since equals checks for q r s
        }

        List<Hex> validPositions = new ArrayList<>();
        HashSet<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new LinkedList<>();
        Hex startTile = boardState.getRandomPiece().getHex();
        assert startTile != null; // something went wrong setting up the board
        queue.offer(startTile);

        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            visited.add(current);

            // ignore hexes of opposite colour, still visit neighbouring tiles
            if (boardState.getPieceAt(current).getColour() != colour) {
                for (Hex neighbour : current.getNeighbours()) {
                    if (!visited.contains(neighbour) && boardState.hasTileAtHex(neighbour)) {
                        queue.offer(neighbour);
                    }
                }
            } else {
                assert colour == boardState.getPieceAt(current).getColour();

                for (Hex neighbour : current.getNeighbours()) {
                    if (visited.contains(neighbour)) continue;

                    if (boardState.hasTileAtHex(neighbour)) {
                        queue.offer(neighbour);
                    } else {
                       if (isValidPlacePosition(boardState, colour, neighbour)) validPositions.add(neighbour);
                    }
                }
            }
        }
        return validPositions;
    }

    private boolean isValidPlacePosition(HiveBoardState<Hex, HiveTile> boardState, HiveColour colour, Hex hex) {
        assert !boardState.hasPieceAt(hex); // should be empty hex
        for (Hex neighbour : hex.getNeighbours()) {
            if (!boardState.hasPieceAt(neighbour)) continue;

            if (boardState.getPieceAt(neighbour).getColour() != colour) {
                return false;
            }
        }

        return true;
    }

    public boolean isFreeToMove(HiveBoardState<Hex, HiveTile> boardState, HiveTile tileToMove, Hex nextPosition) {
        // Move must belong to board
        assert boardState.getPieceAt(tileToMove.getHex()).equals(tileToMove);
        Hex direction = Hex.hexSubtract(tileToMove.getHex(), nextPosition);
        int directionIndex = Hex.hexDirectionAsIndex(direction);

        boolean leftDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(tileToMove.getHex(), (directionIndex + 1) % 6));
        boolean rightDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(tileToMove.getHex(), (directionIndex - 1) % 6));

        return leftDirectionClear || rightDirectionClear;
    }

    // alternative, might be better if using direction index instead of full move?
    public boolean isFreeToMove(HiveBoardState<Hex, HiveTile> boardState, HiveTile hiveTile, int directionIndex) {
        // Move must belong to board
        assert boardState.getPieceAt(hiveTile.getHex()).equals(hiveTile);

        boolean leftDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(hiveTile.getHex(), (directionIndex + 1) % 6));
        boolean rightDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(hiveTile.getHex(), (directionIndex - 1) % 6));

        return leftDirectionClear || rightDirectionClear;
    }

    // BFS and check if all states visited, allows for skipping a tile (for oneHiveMoving)
    public boolean isConnected(HiveBoardState<Hex, HiveTile> boardState, Hex startingTile, Hex tileToSkip) {
        if (startingTile == null) return true;

        HashSet<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new LinkedList<>();
        queue.offer(startingTile);
        visited.add(tileToSkip);

        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            visited.add(current);

            for (Hex neighbour : current.getNeighbours()) {
                if (neighbour.equals(tileToSkip)) continue;

                if (boardState.hasPieceAt(neighbour) && !visited.contains(neighbour)) {
                    queue.offer(neighbour);
                }
            }
        }

        return visited.containsAll(boardState.getAllPositions());
    }

    public boolean isOneHive(HiveBoardState<Hex, HiveTile> boardState) {
        Hex tile = boardState.isBoardEmpty() ? null : boardState.getRandomPiece().getHex();
        return isConnected(boardState, tile, null);
    }

    public boolean isOneHiveWhileMoving(HiveBoardState<Hex, HiveTile> boardState, HiveTile hiveTile) {
        Hex tile = boardState.isBoardEmpty() ? null : boardState.getRandomPiece().getHex();
        return isConnected(boardState, tile, hiveTile.getHex());
    }
}
