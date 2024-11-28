package io.github.finnperera.playmodular.initialframework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MapBasedStorage<P, T> implements BoardStorage<P, T>{
    private final Map<P, T> board;

    public MapBasedStorage() {
        this.board = new HashMap<>();
    }

    // shallow copy
    public MapBasedStorage(MapBasedStorage<P, T> other) {
        this.board = new HashMap<>(other.board);
    }

    // deep copy
    public MapBasedStorage(MapBasedStorage<P, T> other, Function<T, T> deepCopyFunction) {
        this.board = new HashMap<>();
        for (Map.Entry<P, T> entry : other.board.entrySet()) {
            this.board.put(entry.getKey(), deepCopyFunction.apply(entry.getValue()));
        }
    }

    @Override
    public T getPieceAt(P position) {
        return board.get(position);
    }

    @Override
    public void placePieceAt(P position, T newPiece) {
        assert !board.containsKey(position): "Placing piece on occupied position";
        board.put(position, newPiece);
    }

    @Override
    public void removePieceAt(P position) {
        assert board.containsKey(position): "Removing from non-occupied position";
        board.remove(position);
    }

    @Override
    public List<P> getAllPositions() {
        return new ArrayList<>(board.keySet());
    }

    @Override
    public List<T> getAllPieces() {
        return new ArrayList<>(board.values());
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
