package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HiveHeuristics.BasicHeuristic;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HiveGame implements Game<Hex, HiveTile>, ConfigurableOptions {

    private final HiveRuleEngine ruleEngine;
    private final HiveBoardState boardState;
    private final HivePlayer player1;
    private final HivePlayer player2;
    private int turn;
    private final BasicHeuristic heuristic = new BasicHeuristic(); // change when generalising

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
            List<HiveMove> pieceMoves = ruleEngine.generatePieceMoves(boardState, piece);
            moves.addAll(pieceMoves.stream().filter(this::isValidMove).toList());
        });

        Collections.shuffle(moves);
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

    @Override
    public HiveGame makeMove(Move<Hex, HiveTile> move) {
        if (move == null) {
            throw new IllegalArgumentException("Move cannot be null");
        }

        if (move instanceof HiveMove hiveMove) {
            return processHiveMove(hiveMove);
        } else {
            throw new IllegalArgumentException("Invalid move type: " + move.getClass().getName());
        }
    }

    private HiveGame processHiveMove(HiveMove move) {
        if (!isValidMove(move)) {
            throw new IllegalArgumentException("Invalid move: " + move.getClass().getName());
        }

        HiveGame newGame;

        if (move.isPlacementMove()) {
            HiveColour currentPlayerCol = getCurrentPlayer().getColour();
            Player player = getCurrentPlayer();
            if (player instanceof HiveAI ai) {
                player = new HiveAI(ai.removeTile(move.getPieceToMove().getTileType()), ai.getColour(), ai.getModel());
            } else {
                player = new HivePlayer(((HivePlayer) player).removeTile(move.getPieceToMove().getTileType()), currentPlayerCol);
            }

            HiveBoardState newBoardState = new HiveBoardState(boardState);
            newBoardState.placePiece(move.getNextPosition(), move.getPieceToMove());

            if (player1.getColour() == currentPlayerCol) {
                newGame = new HiveGame(ruleEngine, (HivePlayer) player, player2, newBoardState, turn + 1);
            } else {
                newGame = new HiveGame(ruleEngine, player1, (HivePlayer) player, newBoardState, turn + 1);
            }
        } else {
            HiveBoardState newBoardState = new HiveBoardState(boardState);
            HiveTile newTile = new HiveTile(move.getPieceToMove().getTileType(), move.getNextPosition(), move.getPieceToMove().getColour());
            newBoardState.removePieceAt(move.getPieceToMove().getHex());
            newBoardState.placePiece(move.getNextPosition(), newTile);
            newGame = new HiveGame(ruleEngine, player1, player2, newBoardState, turn + 1);
        }
        return newGame;
    }

    @Override
    public int evaluateBoardState(BoardState<Hex, HiveTile> boardState) {
        return heuristic.getEvaluation(this);
    }

    // could optimise greatly
    @Override
    public boolean isValidMove(Move<Hex, HiveTile> move) {
        if (move == null) {
            throw new IllegalArgumentException("Move cannot be null");
        }

        if (move instanceof HiveMove hiveMove) {
            return validateHiveMove(hiveMove);
        } else {
            throw new IllegalArgumentException("Invalid move type: " + move.getClass().getName());
        }
    }

    private boolean validateHiveMove(HiveMove hiveMove) {
        HivePlayer player = getCurrentPlayer();
        HiveColour colour = player.getColour();
        if (hiveMove.getPieceToMove().getColour() != colour) return false;

        if (hiveMove.isPlacementMove()) {
            return ruleEngine.isValidPlacePosition(boardState, colour, hiveMove.getNextPosition());
        }

        //boolean queenPlaced = getBoardState().getQueenOfPlayer(player) != null;
        return getBoardState().getQueenOfPlayer(player) != null;
        // ruleEngine.generatePieceMoves(boardState,
        //                hiveMove.getPieceToMove()).stream().anyMatch(Predicate.isEqual(hiveMove))
    }


    // basic terminal check, needs improvement
    @Override
    public boolean isTerminalState() {
        if (boardState.getPieceCount() < 7) return false; // No queen can be surrounded
        for (HiveTile queen : boardState.getQueens()) {
            // if every hex surrounding queen has a tile
            int hasTile = 0;
            for (Hex hex : queen.getHex().getNeighbours()) {
                if (boardState.hasPieceAt(hex)) {
                    hasTile++;
                }
            }
            if (hasTile == 6) return true;
        }
        return false;
    }

    @Override
    public GameResult getGameResult(Player player) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        GameResult result = null;
        HivePlayer hivePlayer = (HivePlayer) player;

        for (HiveTile queen : boardState.getQueens()) {
            int hasTile = 0;
            for (Hex hex : queen.getHex().getNeighbours()) {
                if (boardState.hasPieceAt(hex)) {
                    hasTile++;
                }
            }
            if (hasTile == 6) {
                if (result == null) {
                    result = queen.getColour() == hivePlayer.getColour() ? GameResult.LOSS : GameResult.WIN;
                } else {
                    result = GameResult.DRAW;
                }
            }
        }
        return result;
    }

    @Override
    public HiveGame handleNoAvailableMoves() {
        this.nextTurn();
        if (getAvailableMoves(getCurrentPlayer()).isEmpty()) {
            // somehow return terminal? Could send null and just assume null == draw?
            return null;
        }
        return this;
    }

    @Override
    public List<Option<?>> getOptions() {
        return List.of();
    }

    @Override
    public void setOptions(List<Option<?>> options) {}


    public HiveBoardState getBoardState() {
        return boardState;
    }

    @Override
    public HivePlayer getCurrentPlayer() {
        return turn % 2 == 0 ? player2 : player1;
    }

    @Override
    public HivePlayer getCurrentOpponent() {
        return turn % 2 == 0 ? player1 : player2;
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
