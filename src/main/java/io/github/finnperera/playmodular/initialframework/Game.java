package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface Game {

    public List<Move> getMoves();

    public BoardState makeMove(BoardState boardState, Move move);
    
    public int evaluateBoardState(BoardState boardState); // must discern player?

    public List<Option> getPossibleOptions();

    public boolean isValidMove(BoardState boardState, Move move);

    public boolean isTerminalState(BoardState boardState);
}
