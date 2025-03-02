package io.github.finnperera.playmodular.initialframework.AIModels.Minimax;

import io.github.finnperera.playmodular.initialframework.Game;
import io.github.finnperera.playmodular.initialframework.Move;

import java.util.ArrayList;
import java.util.List;

public class MinimaxNode<P, T> {
    private final Game<P, T> gameState;
    private final MinimaxNode<P, T> parent;
    private final Move<P, T> moveMade;
    private final List<MinimaxNode<P, T>> children;
    private int value;

    public MinimaxNode(Game<P, T> gameState, MinimaxNode<P, T> parent, Move<P, T> moveMade) {
        this.gameState = gameState;
        this.parent = parent;
        this.moveMade = moveMade;
        this.children = new ArrayList<>();
    }

    public List<MinimaxNode<P, T>> getChildren() {
        return children;
    }

    public Game<P, T> getGameState() {
        return gameState;
    }

    public Move<P, T> getMoveMade() {
        return moveMade;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
