package io.github.finnperera.playmodular.initialframework;

public abstract class Move<P, T> {
    protected T pieceToMove;
    protected P nextPosition;

    public T getPieceToMove() {
        return pieceToMove;
    }

    public P getNextPosition() {
        return nextPosition;
    }
}
