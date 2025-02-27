package io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/*
    Two ways to do MCTS:
    1. Recreate tree after every made move, root is current game state
    2. Continue with old tree, prune other branches then continue using prior context
    Ways to pick final move:
    1. Most visits (most stable)
    2. Highest win rate (highest quality)
    3. Custom heuristic / mix of previous ways
 */
public class MonteCarloModel<P, T> implements AI<P, T> {

    public static final double EXPLORATION_CONSTANT = 1.27; // Constant factor for UCB (sqrt(2) is a common val)
    public static final int MAX_MOVES = 150;
    private final int iterations;
    private MCTSNode<P, T> rootNode;

    public MonteCarloModel(int iterations) {
        this.iterations = iterations;
    }

    /*
    need to prune before doing most of this?
     */
    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        rootNode = new MCTSNode<>(game, null, moves, null); // set root as current game state

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>(numThreads);

        for (int i = 0; i < iterations; i++) {
            futures.add(executor.submit(() -> {
                MCTSNode<P, T> selectedNode = select();
                MCTSNode<P, T> expandedNode = expand(selectedNode);
                MCTSNode<P, T> selectedExpansion = getBestChild(expandedNode);
                Game<P, T> finalState = simulate(selectedExpansion);
                backpropagation(finalState, selectedExpansion);
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return getBestMove();
    }

    // traverse tree, select best ucb until leaf or unexpanded.
    private MCTSNode<P, T> select() {
        System.out.println("Select Stage");
        MCTSNode<P, T> current = rootNode;

        while (!current.getChildren().isEmpty() && !current.getUntriedMoves().isEmpty()) {
            current = getBestChild(current);
        }

        return current;
    }

    private synchronized MCTSNode<P, T> getBestChild(MCTSNode<P, T> node) {
        MCTSNode<P, T> bestChild = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (MCTSNode<P, T> child : node.getChildren()) {
            double score = calculateUCB(child);
            if (score > bestScore) {
                bestChild = child;
                bestScore = calculateUCB(child);
            }
        }

        if (bestChild == null) {
            System.out.println("No best child");
        }

        return bestChild;
    }

    private synchronized MCTSNode<P, T> expand(MCTSNode<P, T> node) {
        System.out.println("Expand Stage");

        if (node.getGameState().isTerminalState()) return node; // end node
        if (!node.getChildren().isEmpty()) return node; // already expanded

        node.expand();
        return node;
    }

    private Game<P, T> simulate(MCTSNode<P, T> node) {
        System.out.println("Simulate Stage");
        Game<P, T> game = node.getGameState();
        int depth = 0;
        while (!game.isTerminalState() && depth < MAX_MOVES) {
            List<? extends Move<P, T>> availableMoves = game.getAvailableMoves(game.getCurrentPlayer());

            if (availableMoves.isEmpty()) {
                game = game.handleNoAvailableMoves();
            } else {
                Move<P, T> nextMove = availableMoves.get(ThreadLocalRandom.current().nextInt(availableMoves.size()));
                game = game.makeMove(nextMove);
            }

            depth++;
        }
        System.out.println("Simulation finished with depth: " + depth);

        return game; // return final result
    }

    private void backpropagation(Game<P, T> game, MCTSNode<P, T> returnNode) {
        System.out.println("BackPropagating Stage");
        Player lastPlayer = game.getCurrentOpponent();
        double score;
        if (game.isTerminalState()) {
            GameResult result = game.getGameResult(rootNode.getGameState().getCurrentPlayer());
            if (result == GameResult.WIN) {
                score = 1;
            } else if (result == GameResult.LOSS) {
                score = -1;
            } else {
                score = 0;
            }
        } else { // game timed out
            score = 0;
        }

        MCTSNode<P, T> current = returnNode;
        while (current != null) {
            synchronized (current) {
                    boolean isMaxPlayer = current.getGameState().getCurrentPlayer().equals(lastPlayer);
                    current.addValue(isMaxPlayer ? score : 1.0 - score);
            }
            current = current.getParent();
        }

    }

    private Move<P, T> getBestMove() {
        // fallback
        if (rootNode.getChildren().isEmpty()) {
            System.out.println("RootNode does not have children");
            List<? extends Move<P, T>> availableMoves = rootNode.getGameState().getAvailableMoves(rootNode.getGameState().getCurrentPlayer());
            return availableMoves.get(ThreadLocalRandom.current().nextInt(availableMoves.size()));
        }

        // For maximising visits
        /*int highestVisits = -1;
        Move<P, T> bestMove = null;
        for (MCTSNode<P, T> node : rootNode.getChildren()) {
            if (node.getVisits() > highestVisits) {
                highestVisits = node.getVisits();
                bestMove = node.getMoveMade();
            }
        }*/

        // For Maximising Score
        double highestScore = Double.NEGATIVE_INFINITY;
        Move<P, T> bestMove = null;
        for (MCTSNode<P, T> node : rootNode.getChildren()) {
            if (node.getTotalValue() > highestScore) {
                highestScore = node.getTotalValue();
                bestMove = node.getMoveMade();
            }
        }
        System.out.println("Best Move Score: " + highestScore);
        return bestMove;
    }

    private double calculateUCB(MCTSNode<P, T> node) {
        if (node.getVisits() == 0) {
            return Double.POSITIVE_INFINITY;
        }

        double v = node.getTotalValue() / node.getVisits();
        double explorationTerm = Math.sqrt(Math.log(node.getParent().getVisits()) / node.getVisits());

        return v + EXPLORATION_CONSTANT * explorationTerm;
    }
}
