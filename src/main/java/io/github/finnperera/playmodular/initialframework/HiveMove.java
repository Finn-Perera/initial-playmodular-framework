package io.github.finnperera.playmodular.initialframework;

public class HiveMove extends Move {
    private HiveTile tileToMove;
    private Hex nextPosition;

    // original tile
    // new position
    public HiveMove(HiveTile tileToMove, Hex nextPosition) {
        this.tileToMove = tileToMove;
        this.nextPosition = nextPosition;
    }

    public HiveTile getTileToMove() {
        return tileToMove;
    }

    public Hex getNextPosition() {
        return nextPosition;
    }
}
