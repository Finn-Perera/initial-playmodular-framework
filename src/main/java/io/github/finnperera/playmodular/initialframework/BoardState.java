package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface BoardState<P, T> {
    public T getPieceAt(P position);
    public void placePiece(P position, T piece);
    public void removePieceAt(P position);
    public List<P> getAllPositions();
    public boolean hasPieceAt(P position);
    public boolean isBoardEmpty();
    public int getPieceCount(); // maybe needs to be renamed
    public T getRandomPiece();
}
