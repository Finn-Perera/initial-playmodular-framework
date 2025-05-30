package io.github.finnperera.playmodular.initialframework;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HiveMove extends Move<Hex, HiveTile> {

    private boolean placementMove;

    public HiveMove(HiveTile tileToMove, Hex nextPosition, boolean placementMove) {
        this.pieceToMove = tileToMove;
        this.nextPosition = nextPosition;
        this.placementMove = placementMove;
    }

    @Override
    public HiveTile getPieceToMove() {
        return super.getPieceToMove();
    }

    @Override
    public Hex getNextPosition() {
        return super.getNextPosition();
    }

    public boolean isPlacementMove() {
        return placementMove;
    }

    @Override
    public String toString() {
        return "HiveMove{" +
                "placementMove=" + placementMove +
                ", pieceToMove=" + pieceToMove +
                ", nextPosition=" + nextPosition +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(placementMove, getPieceToMove(), getNextPosition());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HiveMove hiveMove = (HiveMove) o;
        return placementMove == hiveMove.placementMove &&
                Objects.equals(pieceToMove, hiveMove.pieceToMove) &&
                Objects.equals(nextPosition, hiveMove.nextPosition);
    }

    @Override
    public Map<String, Object> toLogMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("pieceToMove", pieceToMove.toLogMap());
        map.put("nextPosition", nextPosition.toString());
        map.put("placementMove", placementMove);
        return map;
    }
}
