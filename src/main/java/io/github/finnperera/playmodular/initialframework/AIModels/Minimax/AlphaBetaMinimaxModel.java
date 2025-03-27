package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.List;

public class AlphaBetaMinimaxModel<P, T> implements AI<P, T> {

    private static final int MAX_DEPTH = 4;
    private final Player maxPlayer;
    private final Heuristic<P, T> heuristic;

    public AlphaBetaMinimaxModel(Player maxPlayer, Heuristic<P, T> heuristic) {
        this.maxPlayer = maxPlayer;
        this.heuristic = heuristic;
    }

    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        //MinimaxNode<P, T> rootNode =  new MinimaxNode<>(game, null, null);

        System.out.println("Beginning Alpha-Beta Minimax:\n");
        Move<P, T> bestMove = null;
        int bestVal = Integer.MIN_VALUE;

        for (Move<P, T> move : moves) {
            Game<P, T> newState = game.makeMove(move);
            int moveVal = minimax(newState, Integer.MIN_VALUE, Integer.MAX_VALUE, MAX_DEPTH, false);

            if (moveVal > bestVal) {
                bestVal = moveVal;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int minimax(Game<P,T> gameState, int alpha, int beta, int depth, boolean maxPlayer) {
        if (depth <= 0 || gameState.isTerminalState()) {
            return heuristic.getEvaluation(gameState, this.maxPlayer);
        }

        // expand here since depth != 0 and not terminal?
        List<? extends Move<P, T>> moves = gameState.getAvailableMoves(gameState.getCurrentPlayer());
        if (moves.isEmpty()) {
            // this line could cause problems?
            return minimax(gameState.handleNoAvailableMoves(), depth - 1, alpha, beta, !maxPlayer);
        }
//        } else {
//            for (Move<P, T> move : moves) {
//                node.getChildren().add(new MinimaxNode<>(node.getGameState().makeMove(move), node, move));
//            }
//        }

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
}