package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface Game<P, T> { // P : Position, T : Tile/Piece
    public List<? extends Move> getAvailableMoves(Player player);

    public BoardState<P, T> makeMove(BoardState<P, T> boardState, Move move);
    
    public int evaluateBoardState(BoardState<P, T> boardState); // must discern player?

    public List<Option> getPossibleOptions();

    public boolean isValidMove(BoardState<P, T> boardState, Move move);

    public boolean isTerminalState(BoardState<P, T> boardState);
}
