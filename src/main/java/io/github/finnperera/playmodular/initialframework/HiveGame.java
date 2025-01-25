package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HiveGame implements Game<Hex, HiveTile> {

    private final HiveRuleEngine ruleEngine;
    private HivePlayer player1;
    private HivePlayer player2;
    private final HiveBoardState boardState;
    private int turn;

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

    public List<HiveMove> getPlacementMoves(HivePlayer player) {
        List<HiveMove> moves = new ArrayList<>();
        List<Hex> placementPositions = ruleEngine.generatePlacementPositions(boardState, player);

        // could be a bit slow
        if (boardState.getAllPiecesOfPlayer(player).size() >= 3 && boardState.getQueenOfPlayer(player) == null) {
            for (Hex placementPosition : placementPositions) {
                moves.add(new HiveMove(new HiveTile(HiveTileType.QUEEN_BEE, placementPosition, player.getColour()), placementPosition, true));
            }
            return moves;
        }

        for (HiveTileType type : HiveTileType.values()) {
            if (player.getTypeRemainingTiles(type) < 1) continue;
            for (Hex placementPosition : placementPositions) {
                // this might need changing VV
                moves.add(new HiveMove(new HiveTile(type, placementPosition, player.getColour()), placementPosition, true));
            }
        }
        return moves;
    }

    // Does not work currently
    @Override
    public BoardState<Hex, HiveTile> makeMove(BoardState<Hex, HiveTile> boardState, Move move) {
        HiveGame newGame = makeMove((HiveMove) move);
        player1 = newGame.player1;
        player2 = newGame.player2;
        return newGame.getBoardState();

        /*assert boardState instanceof HiveBoardState: "BoardState is not of type HiveBoardState";
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
        return newBoardState;*/
    }

    public HiveGame makeMove(HiveMove move) {
        if (!isValidMove(boardState, move)){
            return null; // error?
        }
        HiveGame newGame;

        if (move.isPlacementMove()) {
            HiveColour currentPlayerCol = getCurrentPlayer().getColour();
            HivePlayer player = new HivePlayer(getCurrentPlayer().removeTile(move.getPieceToMove().getTileType()), currentPlayerCol);
            HiveBoardState newBoardState = new HiveBoardState(boardState);
            newBoardState.placePiece(move.getNextPosition(), move.getPieceToMove());
            nextTurn();
            if (player1.getColour() == currentPlayerCol) {
                newGame = new HiveGame(ruleEngine, player, player2, newBoardState, turn);
            } else {
                newGame = new HiveGame(ruleEngine, player1 , player, newBoardState, turn);
            }
        } else {
            HiveBoardState newBoardState = new HiveBoardState(boardState);
            HiveTile newTile = new HiveTile(move.getPieceToMove().getTileType(), move.getNextPosition(), move.getPieceToMove().getColour());
            newBoardState.removePieceAt(move.getPieceToMove().getHex());
            newBoardState.placePiece(move.getNextPosition(), newTile);
            nextTurn();
            newGame = new HiveGame(ruleEngine, player1, player2, newBoardState, turn);
        }
        return newGame;
    }

    @Override
    public int evaluateBoardState(BoardState<Hex, HiveTile> boardState) {
        return 0;
    }

    @Override
    public List<Option> getPossibleOptions() {
        return List.of();
    }

    // could optimise greatly
    @Override
    public boolean isValidMove(BoardState<Hex, HiveTile> boardState, Move move) {
        HiveMove hiveMove = (HiveMove) move; // needs changing
        HivePlayer player = getCurrentPlayer();
        HiveColour colour = player.getColour();
        if (hiveMove.getPieceToMove().getColour() != colour) return false;

        if (hiveMove.isPlacementMove()) {
            return getPlacementMoves(player).stream().anyMatch(Predicate.isEqual(hiveMove));
        }

        // PROBLEM - using board state of class, not passed in
        boolean queenPlaced = getBoardState().getQueenOfPlayer(player) != null;
        return queenPlaced && ruleEngine.generatePieceMoves((HiveBoardState) boardState, hiveMove.getPieceToMove()).stream().anyMatch(Predicate.isEqual(hiveMove));
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

    public HiveColour getWinner(HiveGame game) {
        if (!game.isTerminalState(game.getBoardState())) return null; // redundant?
        HiveColour winner = null;
        for (HiveTile queen : game.getBoardState().getQueens()) {
            int hasTile = 0;
            for (Hex hex : queen.getHex().getNeighbours()) {
                if (game.getBoardState().hasPieceAt(hex)) {hasTile++;}
            }
            if (hasTile == 6) {
                winner = winner == null ? queen.getColour() : HiveColour.WHITE_AND_BLACK;
            }
        }
        return winner;
    }

    public HiveBoardState getBoardState() {
        return boardState;
    }

    public HivePlayer getCurrentPlayer() {
        return turn % 2 != 0 ? player1 : player2;
    }

    public int getTurn() {
        return turn;
    }

    public void nextTurn() {
        turn += 1;
    }

    public List<HivePlayer> getPlayers() {
        return List.of(player1, player2);
    }

    public HiveRuleEngine getRuleEngine() {
        return ruleEngine;
    }
}
