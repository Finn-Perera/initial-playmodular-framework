package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HivePanes.HiveGamePane;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;

import java.util.ArrayList;
import java.util.List;

/**
 * The Logic handling playing the game
 * Needed:
 * Game loop? - can do without loop, using events
 */
public class HiveBoardGameController implements TileClickListener, HandClickListener {
    private final HiveGamePane gamePane;
    private HiveGame game;
    private HiveTile selectedTile;
    private HiveColour currentPlayerColour;

    public HiveBoardGameController(HiveGamePane gamePane, HiveGame game) {
        this.gamePane = gamePane;
        this.game = game;

        currentPlayerColour = game.getCurrentPlayer().getColour();

        setListening();
    }

    @Override
    public void onTileClicked(Hex clickedHex) {
        if (clickedHex == null) {
            selectedTile = null;
            showValidMovementFromTile(null);
            return;
        }

        if (selectedTile == null) {
            selectTileOnBoard(clickedHex);
            return;
        }

        if (selectedTile.getHex() == null) { // Effectively checking if placement or not selected
            HiveTile possibleTile = new HiveTile(selectedTile.getTileType(), clickedHex, selectedTile.getColour());
            HiveMove possibleMove = new HiveMove(possibleTile, clickedHex, true);
            if (game.isValidMove(possibleMove)) {
                nextTurn(game.makeMove(possibleMove));
            } else {
                selectTileOnBoard(clickedHex);
            }
        } else {
            // already have a piece on board selected
            HiveMove possibleMove = new HiveMove(selectedTile, clickedHex, false);
            if (game.isValidMove(possibleMove)) {
                nextTurn(game.makeMove(possibleMove));
            } else {
                selectTileOnBoard(clickedHex);
            }
        }
    }

    private void nextTurn(HiveGame nextTurnOfGame) {
        this.game = nextTurnOfGame;
        currentPlayerColour = game.getCurrentPlayer().getColour();

        gamePane.setGame(game);
        gamePane.update();
        setListening();

        // Check for win state
        if (game.isTerminalState()) {
            gamePane.showEndGame();
        } else if (game.getAvailableMoves(game.getCurrentPlayer()).isEmpty()) { // check player can move
            nextTurnOfGame.nextTurn(); // this feels wrong?
            nextTurnOfGame = new HiveGame(nextTurnOfGame.getRuleEngine(), nextTurnOfGame.getPlayers().getFirst(), nextTurnOfGame.getPlayers().getLast(), nextTurnOfGame.getBoardState(), nextTurnOfGame.getTurn());
            if (nextTurnOfGame.getAvailableMoves(nextTurnOfGame.getCurrentPlayer()).isEmpty()) {
                gamePane.showEndGame(GameResult.DRAW); // draw? impossible to reach?
            } else {
                nextTurn(nextTurnOfGame); // shouldn't recur more than once?
            }
        }

        if (game.getCurrentPlayer().isAI() && !game.isTerminalState()) {
            HiveAI aiPlayer = (HiveAI) game.getCurrentPlayer();
            makeAITurn(aiPlayer);
        }

        selectedTile = null;
    }

    private void makeAITurn(HiveAI player) {
        List<HiveMove> availableMoves = game.getAvailableMoves(player);

        HiveMove bestMove = (HiveMove) player.getNextMove(game, availableMoves); // might be problematic

        if (bestMove != null && game.isValidMove(bestMove)) {
            game = game.makeMove(bestMove);
            nextTurn(game);
        }
    }

    private void selectTileOnBoard(Hex clickedHex) {
        HiveTile tile = game.getBoardState().getPieceAt(clickedHex);
        if (tile != null && tile.getColour() == currentPlayerColour) {
            selectedTile = tile;
            showValidMovementFromTile(selectedTile);
        }
    }

    private void showValidMovementFromTile(HiveTile tile) {
        if (tile == null) {
            gamePane.getBoard().highlightPossibleMoves(null);
            return;
        }
        List<Hex> hexList = new ArrayList<>();
        if (tile.getHex() == null) {
            game.getPlacementMoves(game.getCurrentPlayer()).forEach(move -> {
                if (move.getPieceToMove().getTileType() == tile.getTileType()) {
                    hexList.add(move.getNextPosition());
                }
            });
        } else {
            List<HiveMove> moves = game.getRuleEngine().generatePieceMoves(game.getBoardState(), tile);
            for (HiveMove move : moves) {
                if (game.isValidMove(move)) {
                    hexList.add(move.getNextPosition());
                }
            }
        }
        gamePane.getBoard().highlightPossibleMoves(hexList);
    }

    @Override
    public void onItemClicked(HiveTileType tile, HiveColour colour) {
        if (tile == null && colour == null) {
            selectedTile = null;
            showValidMovementFromTile(null);
            return;
        }

        if (currentPlayerColour == colour) {
            selectedTile = new HiveTile(tile, null, colour);
            showValidMovementFromTile(selectedTile);
        }
    }

    private void setListening() {
        gamePane.getBoard().setClickListener(this);
        gamePane.getHandPaneList().forEach(hiveHandPane -> hiveHandPane.setClickListener(this));
    }
}
