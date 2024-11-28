package io.github.finnperera.playmodular.initialframework;

// May want to make this generic
public abstract class Move {
    protected Piece pieceToMove;
    protected Position nextPosition;

    public Piece getPieceToMove() {
        return pieceToMove;
    }

    public Position getNextPosition() {
        return nextPosition;
    }
}
