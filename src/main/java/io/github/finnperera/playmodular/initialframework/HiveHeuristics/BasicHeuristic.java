package io.github.finnperera.playmodular.initialframework.HiveHeuristics;

import io.github.finnperera.playmodular.initialframework.*;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class BasicHeuristic {
    // These variables could be adjusted in menus
    public static int WIN_SCORE = Integer.MAX_VALUE;
    public static int LOSE_SCORE = Integer.MIN_VALUE;
    public static int OWN_QUEEN_SURROUND_SCORE = -50; // Per piece surrounding queen?
    public static int OWN_QUEEN_STUCK_SCORE = -300;
    public static int OPPONENT_QUEEN_SURROUND_SCORE = 50;
    public static int OPPONENT_QUEEN_STUCK_SCORE = 300;

    public static int QUEEN_TILE_VALUE = 70;
    public static int ANT_TILE_VALUE = 80;
    public static int BEETLE_TILE_VALUE = 60;
    public static int GRASSHOPPER_TILE_VALUE = 50;
    public static int SPIDER_TILE_VALUE = 50;


    public int getEvaluation(HiveGame game) {
        HivePlayer maxPlayer = game.getCurrentPlayer();
        HivePlayer minPlayer = game.getCurrentOpponent();
        int evaluationScore = 0;
        evaluationScore += winOrLose(game, maxPlayer);
        evaluationScore += ownQueenSurrounded(game, maxPlayer);
        evaluationScore += opponentQueenSurrounded(game, minPlayer);
        evaluationScore += stuckPieces(game, maxPlayer);

        return evaluationScore;
    }

    private int winOrLose(HiveGame game, HivePlayer player) {
        if (!game.isTerminalState()) return 0;

        GameResult result = game.getGameResult(player);
        return result == GameResult.WIN ? WIN_SCORE : LOSE_SCORE;
    }

    private int ownQueenSurrounded(HiveGame game, HivePlayer maxPlayer) {
        return queenSurround(game, maxPlayer, true);
    }

    private int opponentQueenSurrounded(HiveGame game, HivePlayer minPlayer) {
        return queenSurround(game, minPlayer, false);
    }

    private int queenSurround(HiveGame game, HivePlayer playerWithQueen, boolean isMaxPlayer) {
        int surroundScore;
        int stuckScore;
        if (isMaxPlayer) {
            // Queen is of own player
            surroundScore = OWN_QUEEN_SURROUND_SCORE;
            stuckScore = OWN_QUEEN_STUCK_SCORE;
        } else {
            // Queen is of opponent
            surroundScore = OPPONENT_QUEEN_SURROUND_SCORE;
            stuckScore = OPPONENT_QUEEN_STUCK_SCORE;
        }

        HiveTile queen = game.getBoardState().getQueenOfPlayer(playerWithQueen);
        if (queen == null) return 0;
        int scoreTotal = -surroundScore; // one piece touching queen doesn't affect score? (always will have one)
        for (Hex neighbour : queen.getHex().getNeighbours()) {
            if (game.getBoardState().hasPieceAt(neighbour)) {
                scoreTotal += surroundScore;
            }
        }

        // if queen cannot move
        if (game.getRuleEngine().generatePieceMoves(game.getBoardState(), queen).isEmpty()) {
            scoreTotal += stuckScore;
        }

        return scoreTotal;
    }

    private int stuckPieces(HiveGame game, HivePlayer maxPlayer) {
        // Get neighbours for each piece on the board if > 2 then do one hive rule check
        HiveColour playerColour = maxPlayer.getColour();
        AtomicInteger totalScore = new AtomicInteger();

        // This could probably all be refactored to one loop or put moveable pieces into hashmap?
        // maybe i hash move or the tile that is moving with the board state
        for (Stack<HiveTile> stack : game.getBoardState().getBoard().getAllPieces()) {
            // Pieces stuck under other pieces
            if (stack.size() > 1) {
                stack.subList(0, stack.size() - 1).forEach(stackTile -> {
                    if (stackTile.getColour() == playerColour) {
                        totalScore.addAndGet(-pieceValues(stackTile.getTileType()));
                    } else {
                        totalScore.addAndGet(pieceValues(stackTile.getTileType()));
                    }
                });
            }

            // Neighbouring pieces >= 2
            if (!canBeStuck(stack, game)) continue;

            // If one Hive Rule fails -> piece is stuck
            if (!game.getRuleEngine().isOneHiveWhileMoving(game.getBoardState(), stack.peek())) {
                HiveTile topTile = stack.peek();
                if (topTile.getColour() == playerColour) {
                    totalScore.addAndGet(-pieceValues(topTile.getTileType()));
                } else {
                    totalScore.addAndGet(pieceValues(topTile.getTileType()));
                }
            }
        }
        return totalScore.get();
    }

    private boolean canBeStuck(Stack<HiveTile> stack, HiveGame game) {
        int neighbourCount = 0;
        for (Hex neighbour: stack.peek().getHex().getNeighbours()) {
            if (game.getBoardState().hasPieceAt(neighbour)) {
                neighbourCount++;
            }
            if (neighbourCount > 1) {
                return true;
            }
        }
        return false;
    }

    private int pieceValues(HiveTileType tileType) {
        switch (tileType) {
            case QUEEN_BEE -> {
                return QUEEN_TILE_VALUE;
            }
            case ANT -> {
                return ANT_TILE_VALUE;
            }
            case BEETLE -> {
                return BEETLE_TILE_VALUE;
            }
            case GRASSHOPPER -> {
                return GRASSHOPPER_TILE_VALUE;
            }
            case SPIDER -> {
                return SPIDER_TILE_VALUE;
            }
            default -> throw new UnsupportedOperationException("Unknown tile type: " + tileType);
        }
    }

}
