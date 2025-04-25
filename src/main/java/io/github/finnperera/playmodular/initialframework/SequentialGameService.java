package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HivePanes.HiveGamePane;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SequentialGameService {
    private final Stage stage;
    private final Scene mainMenuScene;
    private final HiveGameConfig gameConfig;
    private final LoggingManager loggingManager;
    private final String logFilePrefix;
    private final boolean shouldLog;
    private final boolean isVisualDisabled;

    private int gamesLeft;

    public SequentialGameService(String logFilePrefix,
                                 boolean shouldLog,
                                 boolean isVisualEnabled,
                                 HiveGameConfig gameConfig,
                                 LoggingManager loggingManager,
                                 Scene mainMenuScene,
                                 Stage stage) {
        this.logFilePrefix = logFilePrefix;
        this.shouldLog = shouldLog;
        this.isVisualDisabled = isVisualEnabled;
        this.gameConfig = gameConfig;
        this.loggingManager = loggingManager;
        this.mainMenuScene = mainMenuScene;
        this.stage = stage;
    }

    public void play(int numGames) {
        this.gamesLeft = numGames;
        playNextGame();
    }

    private void playNextGame() {
        if (gamesLeft <= 0) {
            Platform.runLater(() -> stage.setScene(mainMenuScene));
            return;
        }

        gamesLeft--;

        HiveGame game = gameConfig.createGame();
        HiveGamePane gamePane = new HiveGamePane(game);
        HiveBoardGameController controller = new HiveBoardGameController(gamePane, game);

        if (shouldLog) {
            controller.addGameResultListener(log -> loggingManager.addResultToFiles(logFilePrefix, log));
        }

        if (isVisualDisabled) {
            controller.setOnGameEnd(() -> Platform.runLater(this::playNextGame));
        } else {
            Scene gameScene = new Scene(gamePane, 1280, 640);
            gamePane.setOnMainMenuClicked(() -> Platform.runLater(() -> stage.setScene(mainMenuScene)));
            gamePane.setOnNextGameClicked(this::playNextGame);
            stage.setScene(gameScene);
        }

        controller.beginGame();
    }
}
