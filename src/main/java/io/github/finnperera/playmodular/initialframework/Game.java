package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface Game<P, T> { // P : Position, T : Tile/Piece
    List<? extends Move<P, T>> getAvailableMoves(Player player);

    Game<P, T> makeMove(Move<P, T> move);

    int evaluateBoardState(BoardState<P, T> boardState); // must discern player?

    Game<P, T> handleNoAvailableMoves();

    boolean isValidMove(Move<P, T> move);

    boolean isTerminalState();

    List<Player> getPlayers();

    Player getCurrentPlayer();

    Player getCurrentOpponent();

    GameResult getGameResult(Player player);
}
