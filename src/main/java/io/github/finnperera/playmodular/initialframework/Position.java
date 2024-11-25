package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface Position {
    boolean isAdjacent(Position other);
    List<? extends Position> getNeighbours();
}
