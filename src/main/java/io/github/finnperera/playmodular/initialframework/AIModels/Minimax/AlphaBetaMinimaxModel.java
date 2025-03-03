package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.List;

public class AlphaBetaMinimaxModel<P, T> implements AI<P, T> {

    private final Player maxPlayer;
    private final Heuristic<P, T> heuristic;

    public AlphaBetaMinimaxModel(Player maxPlayer, Heuristic<P, T> heuristic) {
        this.maxPlayer = maxPlayer;
        this.heuristic = heuristic;
    }

    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        MinimaxNode<P, T> rootNode =  new MinimaxNode<>(game, null, null);

        System.out.println("Root Node Created\n Beginning Alpha-Beta Minimax:\n");

        int result = minimax(rootNode, Integer.MIN_VALUE, Integer.MAX_VALUE, 4, true);
        rootNode.setValue(result);

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

    private int minimax(MinimaxNode<P, T> node, int alpha, int beta, int depth, boolean maxPlayer) {
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
                val = Math.max(val, minimax(child, alpha, beta, depth - 1, false));
                alpha = Math.max(alpha, val);

                if (beta <= alpha) break;
            }
        } else {
            val = Integer.MAX_VALUE;
            for (MinimaxNode<P, T> child : node.getChildren()) {
                val = Math.min(val, minimax(child, alpha, beta, depth - 1, true));
                beta = Math.min(beta, val);

                if (beta <= alpha) break;
            }
        }
        System.out.println("Value at depth " + depth + ": " + val);
        node.setValue(val);
        return val;
    }
}