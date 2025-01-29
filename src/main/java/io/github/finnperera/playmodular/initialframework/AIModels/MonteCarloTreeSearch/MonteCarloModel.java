package io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.List;
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

    public static final double EXPLORATION_CONSTANT = 1.41; // Constant factor for UCB (sqrt(2) is a common val)
    public static final int MAX_MOVES = 300;
    private MCTSNode<P, T> rootNode;
    private final int iterations;

    public MonteCarloModel(int iterations) {
        this.iterations = iterations;
    }

    /*
    need to prune before doing most of this?
     */
    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P,T>> moves) {
        rootNode = new MCTSNode<>(game, null, moves, null); // set root as current game state

        for (int i = 0; i < iterations; i++) {
            System.out.println("Iteration: " + i);
            MCTSNode<P, T> selectedNode = select();
            MCTSNode<P, T> expandedNode = expand(selectedNode); // check this
            MCTSNode<P, T> selectedExpansion = getBestChild(expandedNode);
            Game<P, T> finalState = simulate(selectedExpansion);
            backpropagation(finalState, selectedExpansion);
        }


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

    private MCTSNode<P, T> getBestChild(MCTSNode<P, T> node) {
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

    private MCTSNode<P, T> expand(MCTSNode<P, T> node) {
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
            System.out.println("Simulating: depth = " + depth);
            List<? extends Move<P, T>> availableMoves = game.getAvailableMoves(game.getCurrentPlayer());

            if (availableMoves.isEmpty()) {
                game = game.handleNoAvailableMoves();
            } else {
                Move<P, T> nextMove = availableMoves.get(ThreadLocalRandom.current().nextInt(availableMoves.size()));
                game = game.makeMove(nextMove);
            }

            depth++;
        }
        return game; // return final result
    }

    private void backpropagation(Game<P, T> game, MCTSNode<P, T> returnNode) {
        System.out.println("BackPropagating Stage");

        double score;
        if (game.isTerminalState()) {
            GameResult result = game.getGameResult(rootNode.getGameState().getCurrentPlayer());
            if (result == GameResult.WIN) {
                score = 1;
            } else if (result == GameResult.LOSS){
                score = -1;
            } else {
                score = 0;
            }
        } else { // game timed out
            score = 0;
        }

        do {
            returnNode.addValue(score);
            returnNode = returnNode.getParent();
        } while (returnNode != null);
    }

    // Most visits implementation
    private Move<P, T> getBestMove() {
        // fallback
        if (rootNode.getChildren().isEmpty()) {
            System.out.println("RootNode does not have children");
            List<? extends Move<P, T>> availableMoves = rootNode.getGameState().getAvailableMoves(rootNode.getGameState().getCurrentPlayer());
            return availableMoves.get(ThreadLocalRandom.current().nextInt(availableMoves.size()));
        }

        int highestVisits = -1;
        Move<P, T> bestMove = null;
        for (MCTSNode<P, T> node : rootNode.getChildren()) {
            if (node.getVisits() > highestVisits) {
                highestVisits = node.getVisits();
                bestMove = node.getMoveMade();
            }
        }
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
