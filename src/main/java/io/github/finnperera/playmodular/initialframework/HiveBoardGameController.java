package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HivePanes.HiveGamePane;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private List<GameResultListener> gameResultListeners = new ArrayList<>();
    private Instant startTime;
    private Instant startTurnTime;
    private final ArrayList<MoveData> moveList;
    private Runnable onGameEnd; // could incorporate this into a general controller

    public HiveBoardGameController(HiveGamePane gamePane, HiveGame game) {
        this.gamePane = gamePane;
        this.game = game;
        moveList = new ArrayList<>();
        gamePane.setDrawButtonOnClick(this::finishGame);

        setListening();
    }

    public void beginGame() { // might need to add more initial calls here later?
        startTime = Instant.now();
        startTurnTime = Instant.now();
        checkGameState();
    }

    private void handleMove(HiveMove move) {
        Instant endTurnTime = Instant.now();
        game = game.makeMove(move);

        gamePane.setGame(game);
        gamePane.update();
        setListening();

        addToMoveList(move, startTurnTime, endTurnTime);

        startTurnTime = Instant.now();
        checkGameState();
        selectedTile = null;
        System.out.println("Turn: " + game.getTurn());
    }

    private void addToMoveList(HiveMove move, Instant startTime, Instant endTime) {
        moveList.add(new MoveData(java.time.Duration.between(startTime, endTime), move));
    }

    private void checkGameState() {
        if (game.isTerminalState()) {
            finishGame();
            return;
        }

        if (game.getAvailableMoves(game.getCurrentPlayer()).isEmpty()) {
            handleForcedPass();
            return;
        }

        if (game.getCurrentPlayer().isAI()) {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> {
                makeAITurn((HiveAI) game.getCurrentPlayer());
            });
            pause.play();
        }
    }

    private void finishGame() {
        gamePane.showEndGame();
        GameLog gameLog = generateGameLog();
        gameResultListeners.forEach(gameResultListener -> gameResultListener.onGameResult(gameLog));
        if (onGameEnd != null) {
            onGameEnd.run();
        }
    }

    private void handleForcedPass() {
        game = game.handleNoAvailableMoves();
        gamePane.setGame(game);
        gamePane.update();

        if (game.getAvailableMoves(game.getCurrentPlayer()).isEmpty()) {
            gamePane.showEndGame();
        } else {
            checkGameState();
        }
    }

    @Override
    public void onTileClicked(Hex clickedHex) {
        if (clickedHex == null) {
            // clear selected tile
            selectedTile = null;
            showValidMovementFromTile(null);
            return;
        }

        if (selectedTile == null || game.getCurrentPlayer().isAI()) {
            // select tile
            selectTileOnBoard(clickedHex);
            return;
        }

        List<HiveMove> possibleMoves = game.getAvailableMoves(game.getCurrentPlayer());
        HiveMove possibleMove = createMove(clickedHex);

        if (possibleMoves.contains(possibleMove)) {
            handleMove(possibleMove);
        } else {
            selectTileOnBoard(clickedHex);
        }
    }

    private HiveMove createMove(Hex clickedHex) {
        if (selectedTile.getHex() == null) { // check if placement move
            return new HiveMove(new HiveTile(selectedTile.getTileType(), clickedHex, selectedTile.getColour()),
                    clickedHex, true);
        } else {
            return new HiveMove(selectedTile, clickedHex, false);
        }
    }

    private void makeAITurn(HiveAI player) {
        Task<HiveMove> aiTask = new Task<>() {
            @Override
            protected HiveMove call() {
                return (HiveMove) player.getNextMove(game, game.getAvailableMoves(player));
            }
        };

        aiTask.setOnSucceeded(event -> {
            HiveMove aiMove = aiTask.getValue();
            if (aiMove != null) {
                Platform.runLater(() -> handleMove(aiMove));
            }
        });

        aiTask.setOnFailed(event -> {
            aiTask.getException().printStackTrace(); // could expand this
        });

        new Thread(aiTask).start();
    }

    private void selectTileOnBoard(Hex clickedHex) {
        HiveTile tile = game.getBoardState().getPieceAt(clickedHex);
        if (tile != null && tile.getColour() == game.getCurrentPlayer().getColour()) {
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

        if (game.getCurrentPlayer().getColour() == colour) {
            selectedTile = new HiveTile(tile, null, colour);
            showValidMovementFromTile(selectedTile);
        }
    }

    private GameLog generateGameLog() {
        Instant endTime = Instant.now();
        HashMap<Player, GameResult> gameResultMap = new LinkedHashMap<>();

        for (Player player : game.getPlayers()) {
            gameResultMap.put(player, game.getGameResult(player));
        }

        return new GameLog(startTime, endTime, gameResultMap, game.getTurn(), moveList);
    }

    private void setListening() {
        if (gamePane != null) {
            gamePane.setListeners(this);
        }
    }

    public void setOnGameEnd(Runnable callback) {
        this.onGameEnd = callback;
    }

    public void addGameResultListener(GameResultListener gameResultListener) {
        gameResultListeners.add(gameResultListener);
    }
}
