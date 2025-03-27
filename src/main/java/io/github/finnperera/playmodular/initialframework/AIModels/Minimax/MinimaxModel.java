package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.List;

public class MinimaxModel<P, T> implements AI<P, T> {

    private static final int MAX_DEPTH = 2;
    private final Player maxPlayer;
    private final Heuristic<P, T> heuristic;

    public MinimaxModel(Player maxPlayer, Heuristic<P, T> heuristic) {
        this.maxPlayer = maxPlayer;
        this.heuristic = heuristic;
    }

    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        //System.out.println("Beginning minimax:\n");
        Move<P, T> bestMove = null;
        int bestVal = Integer.MIN_VALUE;

        for (Move<P, T> move : moves) {
            Game<P, T> newState = game.makeMove(move);
            int moveVal = minimax(newState, MAX_DEPTH, false);

            if (moveVal > bestVal) {
                bestVal = moveVal;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int minimax(Game<P, T> gameState, int depth, boolean maxPlayer) {
        if (depth <= 0 || gameState.isTerminalState()) {
            return heuristic.getEvaluation(gameState, this.maxPlayer);
        }

        List<? extends Move<P, T>> moves = gameState.getAvailableMoves(gameState.getCurrentPlayer());
        if (moves.isEmpty()) {
            return minimax(gameState.handleNoAvailableMoves(), depth - 1, !maxPlayer);
        }

        int bestVal = maxPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move<P, T> move : moves) {
            Game<P, T> newState = gameState.makeMove(move);
            int val = minimax(newState, depth - 1, !maxPlayer);

            if (maxPlayer) {
                bestVal = Math.max(bestVal, val);
            } else {
                bestVal = Math.min(bestVal, val);
            }
        }

        //System.out.println("Value at depth " + depth + ": " + bestVal);
        return bestVal;
    }
}
