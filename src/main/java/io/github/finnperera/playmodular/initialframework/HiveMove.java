package io.github.finnperera.playmodular.initialframework;

public class HiveMove extends Move {

    private boolean placementMove;

    public HiveMove(HiveTile tileToMove, Hex nextPosition, boolean placementMove) {
        this.pieceToMove = tileToMove;
        this.nextPosition = nextPosition;
        this.placementMove = placementMove;
    }

    @Override
    public HiveTile getPieceToMove() {
        return (HiveTile) super.getPieceToMove();
    }

    @Override
    public Hex getNextPosition() {
        return (Hex) super.getNextPosition();
    }

    public boolean isPlacementMove() {
        return placementMove;
    }
}
