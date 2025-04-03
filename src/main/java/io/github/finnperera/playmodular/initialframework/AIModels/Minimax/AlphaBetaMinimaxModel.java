package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AlphaBetaMinimaxModel<P, T> implements AI<P, T> {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() - 1;
    private static final int MAX_DEPTH = 2;

    private final Player maxPlayer;
    private final Heuristic<P, T> heuristic;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public AlphaBetaMinimaxModel(Player maxPlayer, Heuristic<P, T> heuristic) {
        this.maxPlayer = maxPlayer;
        this.heuristic = heuristic;
    }

    /*
    Implementing multi-threading here can affect the pruning negatively in certain cases
    makes it even more important to have good ordering, which is currently random!
    Still should allow me to go to a lower depth overall.
     */
    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        //System.out.println("Beginning Alpha-Beta Minimax:\n");
        List<Future<EvaluatedMove<P, T>>> futures = new ArrayList<>();

        for (Move<P, T> move : moves) {
            Game<P, T> newState = game.makeMove(move);
            Future<EvaluatedMove<P, T>> future = executor.submit(() ->
                    new EvaluatedMove<>
                            (move, minimax(newState, Integer.MIN_VALUE, Integer.MAX_VALUE, MAX_DEPTH, false)));
            futures.add(future);
        }

        Move<P, T> bestMove = null;
        int bestVal = Integer.MIN_VALUE;

        for (Future<EvaluatedMove<P, T>> future : futures) {
            try {
                EvaluatedMove<P, T> moveEval = future.get();
                if (moveEval.value > bestVal) {
                    bestVal = moveEval.value;
                    bestMove = moveEval.move;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(); // Could expand on this
            }
        }
        return bestMove;
    }

    private int minimax(Game<P, T> gameState, int alpha, int beta, int depth, boolean maxPlayer) {
        if (depth <= 0 || gameState.isTerminalState()) {
            return heuristic.getEvaluation(gameState, this.maxPlayer);
        }

        List<? extends Move<P, T>> moves = gameState.getAvailableMoves(gameState.getCurrentPlayer());
        if (moves.isEmpty()) {
            Game<P, T> noMovesOutcome = gameState.handleNoAvailableMoves();
            if (noMovesOutcome != null) { // temp fix
                return minimax(noMovesOutcome, depth - 1, alpha, beta, !maxPlayer);
            } else {
                return heuristic.getEvaluation(gameState, this.maxPlayer);
            }
        }

        int bestVal = maxPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move<P, T> move : moves) {
            Game<P, T> newState = gameState.makeMove(move);
            int val = minimax(newState, alpha, beta, depth - 1, !maxPlayer);

            if (maxPlayer) {
                bestVal = Math.max(bestVal, val);
                alpha = Math.max(alpha, val);

                if (beta <= alpha) break;
            } else {
                bestVal = Math.min(bestVal, val);
                beta = Math.min(beta, val);

                if (beta <= alpha) break;
            }
        }

        System.out.println("Value at depth " + depth + ": " + bestVal);
        return bestVal;
    }

    public static class EvaluatedMove<P, T> {
        private final Move<P, T> move;
        private final int value;

        public EvaluatedMove(Move<P, T> move, int value) {
            this.move = move;
            this.value = value;
        }
    }
}

