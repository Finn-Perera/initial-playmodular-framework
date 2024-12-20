package io.github.finnperera.playmodular.initialframework;

import java.util.*;

public class HiveRuleEngine {
    public static final int NUM_NEIGHBOURS = 6;

    public HiveRuleEngine() {}

    public List<HiveMove> generatePieceMoves(HiveBoardState boardState, HiveTile hiveTile) {
        List<HiveMove> moves = new ArrayList<>();
        switch (hiveTile.getTileType()) {
            case QUEEN_BEE -> moves = queenMoves(boardState, hiveTile);
            case BEETLE -> moves = queenMoves(boardState, hiveTile);
            case SPIDER -> moves = queenMoves(boardState, hiveTile);
            case ANT -> moves = soldierAntMoves(boardState, hiveTile);
            case GRASSHOPPER -> moves = queenMoves(boardState, hiveTile);
        }
        return moves;
    }

    private List<HiveMove> queenMoves(HiveBoardState boardState, HiveTile hiveTile) {
        // Tile must belong to board
        assert boardState.getPieceAt(hiveTile.getHex()).equals(hiveTile);

        List<HiveMove> moves = new ArrayList<>();
        for (Hex neighbouringTile : hiveTile.getHex().getNeighbours()) {
            // tile empty check
            if (boardState.hasTileAtHex(neighbouringTile)) continue;

            // free to move
            //if (!isFreeToMove(boardState, hiveTile, neighbouringTile)) continue;

            // remains one hive
            // still might not work
            if (!isOneHiveWhileMoving(boardState, hiveTile)) continue;

            // check next position is still connected to hive?
            boolean hasNeighbours = false;
            for (Hex nextNeighbouringTile : neighbouringTile.getNeighbours()) {
                if (hasNeighbours) {continue;}

                if (boardState.hasTileAtHex(nextNeighbouringTile) &&
                        boardState.getPieceAt(nextNeighbouringTile).equals(hiveTile)) {continue;}

                if (boardState.hasPieceAt(nextNeighbouringTile)) {
                    hasNeighbours = true;
                }
            }
            if (!hasNeighbours) {continue;}

            // add move to list
            moves.add(new HiveMove(hiveTile, neighbouringTile, false));
        }
        return moves;
    }

    private List<HiveMove> soldierAntMoves(HiveBoardState boardState, HiveTile hiveTile) {
        if(!isOneHiveWhileMoving(boardState, hiveTile)) return Collections.emptyList();
        List<HiveMove> moves = new ArrayList<>();

        Queue<Hex> queue = new LinkedList<>();
        HashSet<Hex> visited = new HashSet<>();
        queue.add(hiveTile.getHex());
        visited.add(hiveTile.getHex());
        while (!queue.isEmpty()) {
            Hex next =  queue.poll();

            for (Hex hex : next.getNeighbours()) {
                if (boardState.hasTileAtHex(hex)) continue; // if piece skip, cant move into it
                if (visited.contains(hex)) continue;
                visited.add(hex);
                // check if you can move into it
                //isFreeToMove() // wont work if i do it based on hive moves alone.... :( but could just do it off previous hex, maybe need a recursion here?

                // check if it will have at least one neighbour
                boolean neighbourFound = false;
                for (Hex neighbour : hex.getNeighbours()) {
                    if (visited.contains(neighbour)) continue; // if hex is visited
                    if (boardState.hasTileAtHex(neighbour)) neighbourFound = true;
                }
                if (!neighbourFound) continue;

                queue.add(hex);
            }
            if (!next.equals(hiveTile.getHex())) {
                moves.add(new HiveMove(hiveTile, next, false));
            }
        }
        return moves;
    }

    // might not be useful
    private HiveBoardState simulateMove(HiveBoardState boardState, HiveMove move) {
        HiveBoardState newBoard = new HiveBoardState(boardState);
        HiveTile newPiece = new HiveTile(move.getPieceToMove().getTileType(),
                move.getNextPosition(), move.getPieceToMove().getColour());

        newBoard.removePieceAt(move.getPieceToMove().getHex());
        newBoard.placePiece(move.getPieceToMove().getHex(), newPiece);
        return newBoard;
    }

    public List<Hex> generatePlacementPositions(HiveBoardState boardState, HivePlayer player) {
        // not sure how to check this anymore VV
        //assert player.equals(boardState.getPlayer1()) || player.equals(boardState.getPlayer2());
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

    private boolean isValidPlacePosition(HiveBoardState boardState, HiveColour colour, Hex hex) {
        assert !boardState.hasPieceAt(hex); // should be empty hex
        for (Hex neighbour : hex.getNeighbours()) {
            if (!boardState.hasPieceAt(neighbour)) continue;

            if (boardState.getPieceAt(neighbour).getColour() != colour) {
                return false;
            }
        }

        return true;
    }

    public boolean isFreeToMove(HiveBoardState boardState, HiveTile tileToMove, Hex nextPosition) {
        // Move must belong to board
        assert boardState.getPieceAt(tileToMove.getHex()).equals(tileToMove);

        if (Hex.hexDistance(tileToMove.getHex(), nextPosition) > 1) return true;
        Hex direction = Hex.hexSubtract(tileToMove.getHex(), nextPosition);
        int directionIndex = Hex.hexDirectionAsIndex(direction);

        boolean leftDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(tileToMove.getHex(), (directionIndex + 1) % 6));
        boolean rightDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(tileToMove.getHex(), (directionIndex - 1) % 6));

        return leftDirectionClear || rightDirectionClear;
    }

    // alternative, might be better if using direction index instead of full move?
    public boolean isFreeToMove(HiveBoardState boardState, HiveTile hiveTile, int directionIndex) {
        // Move must belong to board
        assert boardState.getPieceAt(hiveTile.getHex()).equals(hiveTile);

        boolean leftDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(hiveTile.getHex(), (directionIndex + 1) % 6));
        boolean rightDirectionClear = boardState.hasTileAtHex(
                Hex.hexNeighbour(hiveTile.getHex(), (directionIndex - 1) % 6));

        return leftDirectionClear || rightDirectionClear;
    }

    // BFS and check if all states visited, allows for skipping a tile (for oneHiveMoving)
    public boolean isConnected(HiveBoardState boardState, Hex startingTile, Hex tileToSkip) {
        if (startingTile == null) return true;

        HashSet<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new LinkedList<>();
        queue.offer(startingTile);
        if (boardState.hasPieceAt(tileToSkip) && tileToSkip != null) {
            visited.add(tileToSkip);
        }

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

    public boolean isOneHive(HiveBoardState boardState) {
        Hex tile = boardState.isBoardEmpty() ? null : boardState.getRandomPiece().getHex();
        return isConnected(boardState, tile, null);
    }

    public boolean isOneHiveWhileMoving(HiveBoardState boardState, HiveTile hiveTile) {
        if (boardState.getAllPositions().size() < 2) {
            return true; // may be wrong?
        }
        Hex tile;
        do {
             tile = boardState.getRandomPiece().getHex();
        } while (hiveTile.getHex() == tile);
        return isConnected(boardState, tile, hiveTile.getHex());
    }
}
