package io.github.finnperera.playmodular.initialframework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapBasedStorage<P, T> implements BoardStorage<P, T>{
    private final Map<P, T> board = new HashMap<>();

    @Override
    public T getPieceAt(P position) {
        return board.get(position);
    }

    @Override
    public void placePieceAt(P position, T newPiece) {
        board.put(position, newPiece);
    }

    @Override
    public void removePieceAt(P position) {
        board.remove(position);
    }

    @Override
    public List<P> getAllPositions() {
        return new ArrayList<>(board.keySet());
    }

    @Override
    public boolean hasPieceAt(P position) {
        return board.containsKey(position);
    }

    @Override
    public boolean isEmpty() {
        return board.isEmpty();
    }

    @Override
    public int size() {
        return board.size();
    }
}
