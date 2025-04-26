package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HivePanes.HiveGamePane;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    What I need here:
    - Menu of games to play (can come later)
    - I need a game instance to be created
    - A visualiser should create a pane which represents the game board
     */
public class Main extends Application implements GameResultListener {
    HiveGameConfig gameConfig = new HiveGameConfig();
    LoggingManager loggingManager = new LoggingManager();
    String filePrefix;
    Stage stage;
    Scene mainMenuScene;

    Button createGameButton = new Button("Create Game");
    CheckBox loggingCheckBox = new CheckBox("Generate Log");
    CheckBox disableVisuals = new CheckBox("Disable Visual");
    CheckBox multiGameCheckBox = new CheckBox("Multiple Games");

    Spinner<Integer> numGamesSpinner;
    Spinner<Integer> simultaneousSimCount;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Pane root = new HBox();
        Scene scene = new Scene(root, 1280, 640);
        stage.setTitle("Hive");
        stage.setScene(scene);

        this.mainMenuScene = scene;

        initialiseGameStateButton(root, scene, stage);
        createPlayerChoiceBox(root, HiveColour.WHITE);
        createPlayerChoiceBox(root, HiveColour.BLACK);
        createGameButton(stage, root);
        stage.show();
    }

    // #BUG - need to update from onGameButtonClicked
    private void initialiseGameStateButton(Pane root, Scene scene, Stage stage) {
        Button createGameState = new Button("Create Game State");
        createGameState.setOnAction(event -> {
            try {
                onDesignGameButtonClicked(scene, stage)
                        .thenAccept(hiveGame -> {
                            prepareGameSetUp(disableVisuals.isSelected(), loggingCheckBox.isSelected());
                            runGamesSequentiallyFromGameState(numGamesSpinner.getValue(),
                                    disableVisuals.isSelected(),
                                    loggingCheckBox.isSelected(),
                                    hiveGame);
                        })
                        .exceptionally(ex -> {
                            System.out.println("Something went wrong: " + ex.getMessage());
                            return null;
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        root.getChildren().add(createGameState);
    }

    public CompletableFuture<HiveGame> onDesignGameButtonClicked(Scene priorScene, Stage stage) throws Exception {
        CompletableFuture<HiveGame> future = new CompletableFuture<>();
        GameDesignerService designerService = new GameDesignerService(stage);

        designerService.openGameDesigner(priorScene, future::complete);
        return future;
    }

    private void createPlayerChoiceBox(Pane root, HiveColour colour) {
        ChoiceBox<String> playerChoiceBox = new ChoiceBox<>();
        playerChoiceBox.getItems().addAll("Human", "Monte Carlo", "Minimax", "Alpha-Beta");
        playerChoiceBox.setValue("Human");

        VBox optionContainer = new VBox();
        optionContainer.setId(colour.name() + " options");

        optionContainer.getChildren().add(getPlayerIDOptionNode(colour,
                gameConfig.configureHivePlayer(colour, playerChoiceBox.getValue())));

        playerChoiceBox.setOnAction(event -> {
            optionContainer.getChildren().clear();

            HivePlayer updatedPlayer = gameConfig.configureHivePlayer(colour, playerChoiceBox.getValue());;
            optionContainer.getChildren().add(getPlayerIDOptionNode(colour, updatedPlayer));

            if (updatedPlayer instanceof HiveAI ai && ai.getAIModel() instanceof ConfigurableOptions config) {
                List<Option<?>> options = config.getOptions();
                gameConfig.setPlayer(colour, updatedPlayer, options);

                optionContainer.getChildren().addAll(createAIOptions(options));
            }
        });

        VBox playerChoiceContainer = new VBox(playerChoiceBox, optionContainer);
        root.getChildren().add(playerChoiceContainer);
    }

    // could be difficulty here?
    private Node getPlayerIDOptionNode(HiveColour colour, HivePlayer player) {
        Option<?> playerID = gameConfig.getPlayerOptions(colour).getFirst();
        List<Option<?>> allOptions = new ArrayList<>();
        allOptions.add(playerID);
        gameConfig.setPlayer(colour, player, allOptions);

        return OptionFactory.createOptionControl(playerID);
    }

    private void createGameButton(Stage stage, Pane root) {
        // creating game option buttons
        VBox multiGameContainer = new VBox();
        numGamesSpinner = new Spinner<>(1, 10000, 1);
        numGamesSpinner.setEditable(true);
        numGamesSpinner.setDisable(true);

        simultaneousSimCount = new Spinner<>(1, Runtime.getRuntime().availableProcessors() / 2, 1);
        simultaneousSimCount.setEditable(true);
        simultaneousSimCount.setDisable(true);

        multiGameSetUp(multiGameContainer, multiGameCheckBox, numGamesSpinner, disableVisuals, simultaneousSimCount);

        createGameButton.setOnAction(event -> {
            int numGames = numGamesSpinner.getValue();
            int threadCount = simultaneousSimCount.getValue();
            boolean shouldLog = loggingCheckBox.isSelected();
            boolean isVisualDisabled = disableVisuals.isSelected();
            boolean isMultiGame = multiGameCheckBox.isSelected();

            prepareGameSetUp(isVisualDisabled, shouldLog);

            if (isMultiGame && isVisualDisabled) {
                runGamesSimultaneously(numGames, threadCount, shouldLog);
            } else {
                runGamesSequentially(numGames, isVisualDisabled, shouldLog);
            }
        });
        root.getChildren().add(createGameButton);
        root.getChildren().add(loggingCheckBox);
        root.getChildren().add(multiGameContainer);
    }

    private void prepareGameSetUp(boolean isVisualDisabled, boolean shouldLog) {
        if (isVisualDisabled && (!gameConfig.getPlayer1().isAI() || !gameConfig.getPlayer2().isAI())) {
            showError("When Humans are playing you must have the visual component enabled", "Lack of Visual");
            return;
        }

        if (shouldLog) {
            filePrefix = loggingManager.setUpSessionLog(gameConfig);
        }
    }

    private void runGamesSimultaneously(int numGames, int threadCount, boolean shouldLog) {
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < numGames; i++) {
                executor.submit(() -> {
                    HiveGame game = gameConfig.createGame();
                    HiveGamePane gamePane = new HiveGamePane(game);
                    HiveBoardGameController controller = new HiveBoardGameController(gamePane, game);

                    if (shouldLog) {
                        enableLogging(controller);
                    }

                    controller.beginGame();
                });
            }
        }
    }

    private void runGamesSequentially(int numGames, boolean isVisualDisabled, boolean shouldLog) {
        SequentialGameService sequentialGameService =
                new SequentialGameService(filePrefix, shouldLog, isVisualDisabled, gameConfig, loggingManager, mainMenuScene, stage);
        sequentialGameService.play(numGames);
    }

    private void runGamesSequentiallyFromGameState(int numGames, boolean isVisualDisabled, boolean shouldLog, HiveGame game) {
        SequentialGameService sequentialGameService =
                new SequentialGameService(filePrefix, shouldLog, isVisualDisabled, gameConfig, loggingManager, mainMenuScene, stage);
        sequentialGameService.play(numGames, game);
    }

    private void multiGameSetUp(Pane container,
                                CheckBox multiGameCheckBox,
                                Spinner<Integer> numGamesSpinner,
                                CheckBox disableVisuals,
                                Spinner<Integer> simultaneousSimCount) {

        VBox spinnerContainer = new VBox(new Label("Number of Games"), numGamesSpinner);
        VBox simCount = new VBox(new Label("Threads Running Simulations"), simultaneousSimCount);

        multiGameCheckBox.setId("checkbox-multi");
        multiGameCheckBox.setOnAction(event -> {
            if (multiGameCheckBox.isSelected()) {
                numGamesSpinner.setDisable(false);
                if (disableVisuals.isSelected()) {
                    simultaneousSimCount.setDisable(false);
                }
            } else {
                numGamesSpinner.setDisable(true);
                simultaneousSimCount.setDisable(true);
            }
        });
        disableVisuals.setOnAction(event ->
                simultaneousSimCount.setDisable(!disableVisuals.isSelected() || !multiGameCheckBox.isSelected()));
        container.getChildren().add(disableVisuals);
        container.getChildren().add(multiGameCheckBox);
        container.getChildren().add(spinnerContainer);
        container.getChildren().add(simCount);
    }

    private void enableLogging(HiveBoardGameController controller) { // needs to be more modular
        controller.addGameResultListener(this);
    }

    private List<Node> createAIOptions(List<Option<?>> options) {
        List<Node> optionNodes = new ArrayList<>();
        for (Option<?> option : options) {
            Node optionControl = OptionFactory.createOptionControl(option);
            optionNodes.add(optionControl);
        }
        return optionNodes;
    }

    private void showError(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void onGameResult(GameLog log) {
        loggingManager.addResultToFiles(filePrefix, log);
    }
}
