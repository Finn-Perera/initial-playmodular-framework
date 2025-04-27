package io.github.finnperera.playmodular.initialframework;

public class Heuristic<P, T> {
    protected String heuristicID;

    public String getHeuristicID() {
        return heuristicID;
    }

    public int getEvaluation(Game<P, T> game) {
        throw new RuntimeException("Not implemented");
    }

    public int getEvaluation(Game<P, T> game, Player player) {
        throw new RuntimeException("Not implemented");
    }
}
