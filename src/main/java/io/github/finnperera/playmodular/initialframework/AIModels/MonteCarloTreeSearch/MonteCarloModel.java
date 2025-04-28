package io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
    Two ways to do MCTS:
    1. Recreate tree after every made move, root is current game state
    2. Continue with old tree, prune other branches then continue using prior context
    Ways to pick final move:
    1. Most visits (most stable)
    2. Highest win rate (highest quality)
    3. Custom heuristic / mix of previous ways
 */
public class MonteCarloModel<P, T> implements AI<P, T>, ConfigurableOptions {

    private static final String OPT_MAX_MOVES = "Maximum Moves";
    private static final String OPT_ITERATIONS = "Iterations";
    private static final String OPT_EXPLO_CONST = "Exploration Constant";

    private static final String DESC_MAX_MOVES = "Number of moves simulated before becoming a draw";
    private static final String DESC_EXPLORATION_CONSTANT =
            "Factor for exploration (high) or exploitation (low) on nodes, typically at sqrt(2)";
    private static final String DESC_ITERATIONS = "Number of game simulations run for each move";
    // Could make these final and have a default value in the options but not set until set options called?
    public double explorationConstant = 1.41; // Constant factor for UCB (sqrt(2) is a common val)
    public int maxMoves = 150;
    private MCTSNode<P, T> rootNode;
    private int iterations = 500;

    public MonteCarloModel() {
    }

    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        rootNode = new MCTSNode<>(game, null, moves, null); // set root as current game state

        int numThreads = Runtime.getRuntime().availableProcessors();
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            List<Future<?>> futures = new ArrayList<>(numThreads);

            for (int i = 0; i < iterations; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        MCTSNode<P, T> selectedNode = select();
                        MCTSNode<P, T> expandedNode = expand(selectedNode);
                        MCTSNode<P, T> selectedExpansion = getBestChild(expandedNode);
                        Game<P, T> finalState = simulate(selectedExpansion);
                        backpropagation(finalState, selectedExpansion);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread was interrupted: " + e.getMessage());
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                }
            }
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
        while (!game.isTerminalState() && depth < maxMoves) {
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

        return game;
    }

    private void backpropagation(Game<P, T> game, MCTSNode<P, T> returnNode) {
        System.out.println("BackPropagating Stage");
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

                boolean isMaxPlayer = current.getGameState().getCurrentPlayer().getPlayerID()
                        .equals(rootNode.getGameState().getCurrentPlayer().getPlayerID());
                current.addValue(!isMaxPlayer ? score : -score);
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

        return v + explorationConstant * explorationTerm;
    }

    @Override
    public List<Option<?>> getOptions() {
        return List.of(
                new Option<>(OPT_MAX_MOVES, DESC_MAX_MOVES, OptionType.SPINNER, Integer.class, maxMoves, 10, 1000),
                new Option<>(OPT_EXPLO_CONST, DESC_EXPLORATION_CONSTANT, OptionType.SPINNER, Double.class, explorationConstant, 0.1, 10.0),
                new Option<>(OPT_ITERATIONS, DESC_ITERATIONS, OptionType.SPINNER, Integer.class, iterations, 1, 10000)
        );
    }

    @Override
    public void setOptions(List<Option<?>> options) {
        for (Option<?> option : options) {
            switch (option.getName()) {
                case OPT_MAX_MOVES:
                    maxMoves = (Integer) option.getValue();
                    break;
                case OPT_EXPLO_CONST:
                    explorationConstant = (Double) option.getValue();
                    break;
                case OPT_ITERATIONS:
                    iterations = (Integer) option.getValue();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option: " + option.getName());
            }
        }
    }

    @Override
    public AI<P, T> copy(Player newPlayer) {
        AI<P, T> copy = new MonteCarloModel<>();
        ConfigurableOptions configurable = (ConfigurableOptions) copy;
        configurable.setOptions(getOptions());
        return copy;
    }
}
