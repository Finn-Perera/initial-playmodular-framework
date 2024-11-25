package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface BoardStorage<P, T> { // P : Position Type, T : Tile/Piece Type
    T getPieceAt(P position);
    void placePieceAt(P position, T newPiece);
    void removePieceAt(P position);
    List<P> getAllPositions();
    boolean hasPieceAt(P position);
    boolean isEmpty();
    int size();
}
