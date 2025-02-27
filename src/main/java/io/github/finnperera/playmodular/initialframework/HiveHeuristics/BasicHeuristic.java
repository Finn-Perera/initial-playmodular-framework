package io.github.finnperera.playmodular.initialframework.HiveHeuristics;

import io.github.finnperera.playmodular.initialframework.GameResult;
import io.github.finnperera.playmodular.initialframework.Hex;
import io.github.finnperera.playmodular.initialframework.HiveGame;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;
import io.github.finnperera.playmodular.initialframework.HiveTile;

public class BasicHeuristic {
    // These variables could be adjusted in menus
    public static int WIN_SCORE = Integer.MAX_VALUE;
    public static int LOSE_SCORE = Integer.MIN_VALUE;
    public static int OWN_QUEEN_SURROUND_SCORE = -50; // Per piece surrounding queen?
    public static int OWN_QUEEN_STUCK_SCORE = -300;
    public static int OPPONENT_QUEEN_SURROUND_SCORE = 50;
    public static int OPPONENT_QUEEN_STUCK_SCORE = 300;


    public int getEvaluation(HiveGame game) {
        HivePlayer maxPlayer = game.getCurrentPlayer();
        HivePlayer minPlayer = game.getCurrentOpponent();
        int evaluationScore = 0;
        evaluationScore += winOrLose(game, maxPlayer);
        evaluationScore += ownQueenSurrounded(game, maxPlayer);
        evaluationScore += opponentQueenSurrounded(game, minPlayer);

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

}
