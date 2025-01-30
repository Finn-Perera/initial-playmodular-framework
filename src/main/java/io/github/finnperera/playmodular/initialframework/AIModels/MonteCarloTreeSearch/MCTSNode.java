package io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch;

import io.github.finnperera.playmodular.initialframework.Game;
import io.github.finnperera.playmodular.initialframework.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MCTSNode<P, T> { // P : Position, T : Tile/Piece

    private final Game<P, T> gameState;
    private final MCTSNode<P, T> parent;
    private List<MCTSNode<P, T>> children;
    private AtomicInteger visits;
    private double totalValue;
    private Move<P, T> moveMade;
    private List<? extends Move<P, T>> untriedMoves;

    public MCTSNode(Game<P, T> gameState, MCTSNode<P, T> parent, List<? extends Move<P, T>> untriedMoves, Move<P, T> moveMade) {
        this.gameState = gameState;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.visits = new AtomicInteger(0);
        this.totalValue = 0.0;
        this.moveMade = moveMade;
        this.untriedMoves = untriedMoves;
    }

    public void expand() {
        for (Move<P, T> move : new ArrayList<>(untriedMoves)) {
            Game<P, T> newGameState = gameState.makeMove(move);
            children.add(new MCTSNode<>(newGameState, this, newGameState.getAvailableMoves(newGameState.getCurrentPlayer()), move));
            untriedMoves.remove(move);
        }
    }

    public synchronized void addValue(double value) {
        this.totalValue += value;
        visits.incrementAndGet();
    }

    public MCTSNode<P, T> getParent() {
        return parent;
    }

    public List<MCTSNode<P, T>> getChildren() {
        return children;
    }

    public int getVisits() {
        return visits.get();
    }

    public synchronized double getTotalValue() {
        return totalValue;
    }

    public Game<P, T> getGameState() {
        return gameState;
    }

    public List<? extends Move<P, T>> getUntriedMoves() {
        return untriedMoves;
    }

    public Move<P,T> getMoveMade() {
        return moveMade;
    }
}
