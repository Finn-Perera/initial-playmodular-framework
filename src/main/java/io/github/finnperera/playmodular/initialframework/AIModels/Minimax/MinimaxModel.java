package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.List;

public class MinimaxModel<P, T> implements AI<P, T> {

    private static final int MAX_DEPTH = 4;
    private final Player maxPlayer;
    private final Heuristic<P, T> heuristic;

    public MinimaxModel(Player maxPlayer, Heuristic<P, T> heuristic) {
        this.maxPlayer = maxPlayer;
        this.heuristic = heuristic;
    }

    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        //MinimaxNode<P, T> rootNode =  new MinimaxNode<>(game, null, null);
        System.out.println("Beginning minimax:\n");
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

        // expand here since depth != 0 and not terminal?
        List<? extends Move<P, T>> moves = gameState.getAvailableMoves(gameState.getCurrentPlayer());
        if (moves.isEmpty()) {
            // this line could cause problems?
            return minimax(gameState.handleNoAvailableMoves(), depth - 1, !maxPlayer);
        }
//        } else {
//            for (Move<P, T> move : moves) {
//                node.getChildren().add(new MinimaxNode<>(node.getGameState().makeMove(move), node, move));
//            }
//        }

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

        /*if (maxPlayer) {
            for (MinimaxNode<P, T> child : node.getChildren()) {
               bestVal  = Math.max(, minimax(child, depth - 1, false));
            }
        } else {
            for (MinimaxNode<P, T> child : node.getChildren()) {
               bestVal  = Math.min(, minimax(child, depth - 1, true));
            }
        }*/
        System.out.println("Value at depth " + depth + ": " + bestVal);
        return bestVal;
    }
}
