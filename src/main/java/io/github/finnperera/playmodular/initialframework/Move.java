package io.github.finnperera.playmodular.initialframework;

public abstract class Move<P, T> implements LoggableComponent {
    protected T pieceToMove;
    protected P nextPosition;

    public T getPieceToMove() {
        return pieceToMove;
    }

    public P getNextPosition() {
        return nextPosition;
    }
}
