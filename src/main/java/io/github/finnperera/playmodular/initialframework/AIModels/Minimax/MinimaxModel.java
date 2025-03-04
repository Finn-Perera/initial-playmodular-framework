package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.List;

public class MinimaxModel<P, T> implements AI<P, T> {

    private final Player maxPlayer;
    private final Heuristic<P, T> heuristic;

    public MinimaxModel(Player maxPlayer, Heuristic<P, T> heuristic) {
        this.maxPlayer = maxPlayer;
        this.heuristic = heuristic;
    }

    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        MinimaxNode<P, T> rootNode =  new MinimaxNode<>(game, null, null);

        System.out.println("Root Node Created\n Beginning minimax:\n");

        minimax(rootNode, 4, true);

        System.out.println("Value returned to root(?): " + rootNode.getValue());

        int bestVal = Integer.MIN_VALUE;
        Move<P, T> bestMove = null;
        for (MinimaxNode<P, T> child : rootNode.getChildren()) {
            if (child.getValue() >= bestVal) {
                bestVal = child.getValue();
                bestMove = child.getMoveMade();
            }
        }
        return bestMove;
    }

    private int minimax(MinimaxNode<P, T> node, int depth, boolean maxPlayer) {
        if (depth == 0 || node.getGameState().isTerminalState()) {
            int score = heuristic.getEvaluation(node.getGameState(), this.maxPlayer);
            node.setValue(score);
            return score;
        }

        // expand here since depth != 0 and not terminal?
        List<? extends Move<P, T>> moves = node.getGameState().getAvailableMoves(node.getGameState().getCurrentPlayer());
        if (moves.isEmpty()) {
            // this line could cause problems?
            node.getChildren().add(new MinimaxNode<>(node.getGameState().handleNoAvailableMoves(), node, null));
        } else {
            for (Move<P, T> move : moves) {
                node.getChildren().add(new MinimaxNode<>(node.getGameState().makeMove(move), node, move));
            }
        }

        int val;

        if (maxPlayer) {
            val = Integer.MIN_VALUE;
            for (MinimaxNode<P, T> child : node.getChildren()) {
                val = Math.max(val, minimax(child, depth - 1, false));
            }
        } else {
            val = Integer.MAX_VALUE;
            for (MinimaxNode<P, T> child : node.getChildren()) {
                val = Math.min(val, minimax(child, depth - 1, true));
            }
        }
        System.out.println("Value at depth " + depth + ": " + val);
        node.setValue(val);
        return val;
    }
}
