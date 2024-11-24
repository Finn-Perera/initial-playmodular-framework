package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface AI {
    public Move getNextMove(List<Move> moves); // maybe needs to have the evaluations for each move?
    // not sure what i should be passing round, moves or board states?
}
