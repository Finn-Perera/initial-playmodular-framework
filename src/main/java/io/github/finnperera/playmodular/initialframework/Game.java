package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface Game<P, T> { // P : Position, T : Tile/Piece
    public List<? extends Move<P, T>> getAvailableMoves(Player player);

    public Game<P, T> makeMove(Move<P, T> move);
    
    public int evaluateBoardState(BoardState<P, T> boardState); // must discern player?

    public Game<P, T> handleNoAvailableMoves();

    public List<Option> getPossibleOptions();

    public boolean isValidMove(Move<P, T> move);

    public boolean isTerminalState();

    public Player getCurrentPlayer();

    public GameResult getGameResult(Player player);
}
