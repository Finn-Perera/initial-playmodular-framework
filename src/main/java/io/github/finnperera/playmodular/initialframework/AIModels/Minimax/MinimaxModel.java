package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MinimaxModel<P, T> implements AI<P, T>, LoggableComponent {

    private static final int MAX_DEPTH = 3;
    private final Player maxPlayer;
    private final Heuristic<P, T> heuristic;
    private int numNodesExplored;

    public MinimaxModel(Player maxPlayer, Heuristic<P, T> heuristic) {
        this.maxPlayer = maxPlayer;
        this.heuristic = heuristic;
        numNodesExplored = 0;
    }

    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        System.out.println("Beginning minimax:\n");
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 2);

        List<Future<EvaluatedMove<P, T>>> futures = new ArrayList<>();

        for (Move<P, T> move : moves) {
            Game<P, T> newState = game.makeMove(move);
            Future<EvaluatedMove<P, T>> future = executor.submit(() ->
                    new EvaluatedMove<>
                            (move, minimax(newState, MAX_DEPTH - 1, false)));
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

    @Override
    public AI<P, T> copy(Player newPlayer) {
        return new MinimaxModel<>(newPlayer, heuristic);
    }

    private int minimax(Game<P, T> gameState, int depth, boolean maxPlayer) {
        numNodesExplored++;
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

        System.out.println("Value at depth " + depth + ": " + bestVal);
        return bestVal;
    }

    @Override
    public Map<String, Object> toLogMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("num explored nodes", numNodesExplored);
        return map;
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
