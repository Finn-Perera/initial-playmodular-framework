package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface AI<P, T> {
    Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves);
}
