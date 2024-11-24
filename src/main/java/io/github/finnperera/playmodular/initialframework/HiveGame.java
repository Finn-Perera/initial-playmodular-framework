package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public class HiveGame implements Game {

    @Override
    public List<Move> getMoves() {
        return List.of();
    }

    @Override
    public BoardState makeMove(BoardState boardState, Move move) {
        assert(isValidMove(boardState, move));
        return null;
    }

    @Override
    public int evaluateBoardState(BoardState boardState) {
        return 0;
    }

    @Override
    public List<Option> getPossibleOptions() {
        return List.of();
    }

    @Override
    public boolean isValidMove(BoardState boardState, Move move) {
        return false;
    }

    @Override
    public boolean isTerminalState(BoardState boardState) {
        return false;
    }
}
