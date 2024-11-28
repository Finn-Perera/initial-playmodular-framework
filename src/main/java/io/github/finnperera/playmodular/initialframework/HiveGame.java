package io.github.finnperera.playmodular.initialframework;

import java.util.ArrayList;
import java.util.List;

public class HiveGame implements Game<Hex, HiveTile> {

    private final HiveRuleEngine ruleEngine;
    private HivePlayer player1;
    private HivePlayer player2;
    private final HiveBoardState boardState;
    private int turn; // 1 or 2

    public HiveGame(HiveRuleEngine ruleEngine, HivePlayer player1, HivePlayer player2, HiveBoardState boardState) {
        this.ruleEngine = ruleEngine;
        this.player1 = player1;
        this.player2 = player2;
        this.boardState = boardState;
        turn = 1;
    }

    public HiveGame(HiveRuleEngine ruleEngine, HivePlayer player1, HivePlayer player2, HiveBoardState boardState, int turn) {
        this.ruleEngine = ruleEngine;
        this.player1 = player1;
        this.player2 = player2;
        this.boardState = boardState;
        this.turn = turn;
    }

    @Override
    public List<HiveMove> getAvailableMoves(Player player) {
        assert player instanceof HivePlayer;
        HivePlayer hivePlayer = (HivePlayer) player;

        List<HiveMove> moves = new ArrayList<>();
        // add all placeable moves
        moves.addAll(getPlacementMoves(hivePlayer));

        // add all current piece moves
        boardState.getAllPiecesOfPlayer(hivePlayer).forEach(piece -> {
            moves.addAll(ruleEngine.generatePieceMoves(boardState, piece));
        });

        return moves;
    }

    private List<HiveMove> getPlacementMoves(HivePlayer player) {
        List<HiveMove> moves = new ArrayList<>();
        List<Hex> placementPositions = ruleEngine.generatePlacementPositions(boardState, player);
        for (HiveTileType type : HiveTileType.values()) {
            if (player.getTypeRemainingTiles(type) < 1) continue;
            for (Hex placementPosition : placementPositions) {
                // this might need changing VV
                moves.add(new HiveMove(new HiveTile(type, placementPosition, player.getColour()), placementPosition, true));
            }
        }
        return moves;
    }

    // doesn't decrement tiles that are placed
    @Override
    public BoardState<Hex, HiveTile> makeMove(BoardState<Hex, HiveTile> boardState, Move move) {
        assert boardState instanceof HiveBoardState: "BoardState is not of type HiveBoardState";
        HiveBoardState newBoardState = new HiveBoardState((HiveBoardState) boardState);

        assert move instanceof HiveMove: "Move is not of type HiveMove";
        HiveMove hiveMove = (HiveMove) move;
        HiveTile pieceToMove = hiveMove.getPieceToMove();

        // needs changing
        if (hiveMove.isPlacementMove()) {
            HivePlayer player = player1.getColour() == hiveMove.getPieceToMove().getColour() ? player1 : player2;
            player.removeTile(pieceToMove.getTileType());
        }

        newBoardState.removePieceAt(pieceToMove.getHex());
        HiveTile newTile = new HiveTile(pieceToMove.getTileType(), hiveMove.getNextPosition(), pieceToMove.getColour());
        newBoardState.placePiece(hiveMove.getNextPosition(), newTile);
        return newBoardState;
    }

    @Override
    public int evaluateBoardState(BoardState<Hex, HiveTile> boardState) {
        return 0;
    }

    @Override
    public List<Option> getPossibleOptions() {
        return List.of();
    }

    @Override
    public boolean isValidMove(BoardState<Hex, HiveTile> boardState, Move move) {
        return false;
    }

    // basic terminal check, needs improvement
    @Override
    public boolean isTerminalState(BoardState<Hex, HiveTile> boardState) {
        assert boardState instanceof HiveBoardState: "BoardState is not of type HiveBoardState";
        HiveBoardState hiveBoardState = new HiveBoardState((HiveBoardState) boardState);
        if (hiveBoardState.getPieceCount() < 7) return false; // No queen can be surrounded
        for (HiveTile queen : hiveBoardState.getQueens()) {
            // if every hex surrounding queen has a tile
            int hasTile = 0;
            for (Hex hex : queen.getHex().getNeighbours()) {
                if (hiveBoardState.hasPieceAt(hex)) {hasTile++;}
            }
            if (hasTile == 6) return true;
        }
        return false;
    }

    public HiveBoardState getBoardState() {
        return boardState;
    }

    public HivePlayer getCurrentPlayer() {
        return turn == 1 ? player1 : player2;
    }

    public int getTurn() {
        return turn;
    }

    public void nextTurn() {
        turn = (turn % 2 == 0) ? 1 : 2;
    }

    public List<HivePlayer> getPlayers() {
        return List.of(player1, player2);
    }
}
