package io.github.finnperera.playmodular.initialframework;

public class HiveMove extends Move {
    public HiveMove(HiveTile tileToMove, Hex nextPosition) {
        this.pieceToMove = tileToMove;
        this.nextPosition = nextPosition;
    }
}
