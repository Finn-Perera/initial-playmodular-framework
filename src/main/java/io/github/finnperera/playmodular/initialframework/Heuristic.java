package io.github.finnperera.playmodular.initialframework;

public interface Heuristic<P, T> {
    int getEvaluation(Game<P, T> game);

    int getEvaluation(Game<P, T> game, Player player);
}
