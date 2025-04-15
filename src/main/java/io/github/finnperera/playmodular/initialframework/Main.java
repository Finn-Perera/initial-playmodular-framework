package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.AlphaBetaMinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.MinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch.MonteCarloModel;
import io.github.finnperera.playmodular.initialframework.HiveHeuristics.BasicHeuristic;
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
import java.util.concurrent.atomic.AtomicReference;

/*
    What I need here:
    - Menu of games to play (can come later)
    - I need a game instance to be created
    - A visualiser should create a pane which represents the game board
     */
public class Main extends Application implements GameResultListener {
    AtomicReference<HivePlayer> player1Selection = new AtomicReference<>(null);
    AtomicReference<HivePlayer> player2Selection = new AtomicReference<>(null);
    List<Option<?>> player1Options = new ArrayList<>();
    List<Option<?>> player2Options = new ArrayList<>();

    LoggingManager loggingManager = new LoggingManager();
    String filePrefix; // this limits the main/client to only run one set of simulations at a time

    private static HiveAI getHiveAI(HiveColour colour, ChoiceBox<String> playerChoiceBox) {
        HiveAI updatedPlayer = null;

        switch (playerChoiceBox.getValue()) {
            case "Monte Carlo" -> updatedPlayer = new HiveAI(colour,
                    new MonteCarloModel<>());
            case "Minimax" -> {
                updatedPlayer = new HiveAI(colour, null);
                updatedPlayer.setModel(new MinimaxModel<>(updatedPlayer, new BasicHeuristic()));
            }
            case "Alpha-Beta" -> {
                updatedPlayer = new HiveAI(colour, null);
                updatedPlayer.setModel(new AlphaBetaMinimaxModel<>(updatedPlayer, new BasicHeuristic()));
            }
        }
        return updatedPlayer;
    }

    @Override
    public void start(Stage stage) {
        Pane root = new HBox();
        Scene scene = new Scene(root, 1280, 640);
        stage.setTitle("Hive");
        stage.setScene(scene);

        initialiseGameStateButton(root, scene, stage);
        createPlayerChoiceBox(root, player1Selection, HiveColour.WHITE);
        createPlayerChoiceBox(root, player2Selection, HiveColour.BLACK);
        createGameButton(stage, root);
        stage.show();
    }

    private void initialiseGameStateButton(Pane root, Scene scene, Stage stage) {
        Button createGameState = new Button("Create Game State");
        createGameState.setOnAction(event -> {
            try {
                onDesignGameButtonClicked(scene, stage)
                        .thenAccept(hiveGame -> {
                            // do something with it?
                            /*HiveBoard game = new HiveBoard(hiveGame);
                            Scene gameScene = new Scene(game, 1280, 640);
                            stage.setScene(gameScene);*/
                            onGameButtonClicked(stage, hiveGame, false);
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

    private void createPlayerChoiceBox(Pane root, AtomicReference<HivePlayer> player, HiveColour colour) {
        ChoiceBox<String> playerChoiceBox = new ChoiceBox<>();
        playerChoiceBox.getItems().addAll("Human", "Monte Carlo", "Minimax", "Alpha-Beta");
        playerChoiceBox.setValue("Human");

        VBox optionContainer = new VBox();
        optionContainer.setId(colour.name() + " options");

        playerChoiceBox.setOnAction(event -> {
            optionContainer.getChildren().clear();

            HiveAI updatedPlayer = getHiveAI(colour, playerChoiceBox);

            Option<?> playerID = updatedPlayer.getOptions().getFirst();

            if (colour == HiveColour.WHITE) {
                player1Options.clear();
                player1Options.add(playerID);
            } else {
                player2Options.clear();
                player2Options.add(playerID);
            }

            player.set(updatedPlayer);
            Node playerIDNode = OptionFactory.createOptionControl(playerID);

            if (updatedPlayer != null) {
                optionContainer.getChildren().add(playerIDNode);
            }

            if (updatedPlayer != null && updatedPlayer.getModel() instanceof ConfigurableOptions configurableOptions) {
                List<Option<?>> options = configurableOptions.getOptions();
                if (colour == HiveColour.WHITE) {
                    player1Options.addAll(options);
                } else {
                    player2Options.addAll(options);
                }
                List<Node> aiOptions = createAIOptions(options); // creates a list of nodes that contain vbox of labels and control nodes
                for (Node aiOption : aiOptions) { // for each of these nodes add them to the container
                    optionContainer.getChildren().add(aiOption);
                }
            }


        });

        VBox playerChoiceContainer = new VBox(playerChoiceBox, optionContainer);
        root.getChildren().add(playerChoiceContainer);
    }

    private void createGameButton(Stage stage, Pane root) {
        Button createGameButton = new Button("Create Game");
        CheckBox loggingCheckBox = new CheckBox("Generate Log");
        Spinner<Integer> numGamesSpinner = new Spinner<>(1, 10000, 1);
        numGamesSpinner.setEditable(true);
        numGamesSpinner.setDisable(true);
        CheckBox disableVisuals = new CheckBox("Disable Visual");
        Spinner<Integer> simultaneousSimCount = new Spinner<>(1, Runtime.getRuntime().availableProcessors() / 2, 1);
        simultaneousSimCount.setEditable(true);
        simultaneousSimCount.setDisable(true);

        CheckBox multiGameCheckBox = new CheckBox("Multiple Games");
        VBox multiGameContainer = new VBox();
        multiGameSetUp(multiGameContainer, multiGameCheckBox, numGamesSpinner, disableVisuals, simultaneousSimCount);

        createGameButton.setOnAction(event -> {
            int numGames = numGamesSpinner.getValue();
            int threadCount = simultaneousSimCount.getValue();
            boolean shouldLog = loggingCheckBox.isSelected();
            boolean isVisualDisabled = disableVisuals.isSelected();
            boolean isMultiGame = multiGameCheckBox.isSelected();

            if (isVisualDisabled && (player1Selection.get() == null || player2Selection.get() == null)) {
                showError("When Humans are playing you must have the visual component enabled", "Lack of Visual");
                return;
            }

            if (shouldLog) {
                filePrefix = loggingManager.setUpSessionLog("HiveGame", 2);
            }

            if (isMultiGame && isVisualDisabled) {
                runGamesSimultaneously(numGames, threadCount, shouldLog);
            } else {
                runGamesSequentially(numGames, isVisualDisabled, shouldLog, stage);
            }
        });
        root.getChildren().add(createGameButton);
        root.getChildren().add(loggingCheckBox);
        root.getChildren().add(multiGameContainer);
    }

    private void runGamesSimultaneously(int numGames, int threadCount, boolean shouldLog) {
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < numGames; i++) {
                executor.submit(() -> {
                    HiveGame game = createGame();
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

    private void runGamesSequentially(int numGames, boolean isVisualDisabled, boolean shouldLog, Stage stage) {
        for (int i = 0; i < numGames; i++) {
            HiveGame game = createGame();
            HiveGamePane gamePane = new HiveGamePane(game);
            HiveBoardGameController controller = new HiveBoardGameController(gamePane, game);

            if (shouldLog) {
                enableLogging(controller);
            }

            if (!isVisualDisabled) {
                Scene gameScene = new Scene(gamePane, 1280, 640);
                Platform.runLater(() -> stage.setScene(gameScene));
            }

            controller.beginGame();
        }
    }

    private HiveGame createGame() {
        HivePlayer player1 = player1Selection.get().copy();
        HivePlayer player2 = player2Selection.get().copy();

        updatePlayerOptions(player1, player1Options);
        updatePlayerOptions(player2, player2Options);

        if (player1 == null) player1 = new HivePlayer(HiveColour.WHITE);
        if (player2 == null) player2 = new HivePlayer(HiveColour.BLACK);

        return new HiveGame(new HiveRuleEngine(), player1, player2, new HiveBoardState());
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

    private void onGameButtonClicked(Stage stage, HiveGame hiveGame, boolean loggingEnabled) {
         /*HiveGame game;
            HivePlayer player1 = player1Selection.get();
            HivePlayer player2 = player2Selection.get();

            updatePlayerOptions(player1, player1Options);
            updatePlayerOptions(player2, player2Options);

            if (player1 == null) player1 = new HivePlayer(HiveColour.WHITE);
            if (player2 == null) player2 = new HivePlayer(HiveColour.BLACK);

            game = new HiveGame(new HiveRuleEngine(), player1, player2, new HiveBoardState());
*/

        /*try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < numGames; i++) {
                final int gameIndex = i;

                executor.submit(() -> {
                    HivePlayer player1 = player1Selection.get().copy();
                    HivePlayer player2 = player2Selection.get().copy();

                    if (player1 == null) player1 = new HivePlayer(HiveColour.WHITE);
                    if (player2 == null) player2 = new HivePlayer(HiveColour.BLACK);

                    updatePlayerOptions(player1, player1Options);
                    updatePlayerOptions(player2, player2Options);

                    HiveGame game = new HiveGame(new HiveRuleEngine(), player1, player2, new HiveBoardState());

                    // should log could be handled here?
                    onGameButtonClicked(stage, game, shouldLog);

                    System.out.println("Game " + gameIndex + " completed");
                });
            }
        }*/


        /*HiveGamePane gamePane = new HiveGamePane(hiveGame);
        HiveBoardGameController controller = new HiveBoardGameController(gamePane, hiveGame);

        if (loggingEnabled) enableLogging(hiveGame, controller); // handle a level higher?

        Scene gameScene = new Scene(gamePane, 1280, 640);
        //Platform.runLater(() -> stage.setScene(gameScene));*/
    }

    private List<Node> createAIOptions(List<Option<?>> options) {
        List<Node> optionNodes = new ArrayList<>();
        for (Option<?> option : options) {
            Node optionControl = OptionFactory.createOptionControl(option);
            optionNodes.add(optionControl);
        }
        return optionNodes;
    }

    private void updatePlayerOptions(HivePlayer player, List<Option<?>> options) { // given a player
        Option<?> playerIDOption = null;

        for (Option<?> option : options) {
            if ("Player ID".equals(option.getName())) {
                playerIDOption = option;
            }
        }

        if (playerIDOption != null) {
            player.setOptions(List.of(playerIDOption));
            options.remove(playerIDOption);
        }


        if (player instanceof HiveAI hiveAI) { // if the player is a hiveAI
            if (hiveAI.getModel() instanceof ConfigurableOptions configurablePlayer) { // does the hive ai have options
                configurablePlayer.setOptions(options); // set these options, which feels redundant?
            }
        }
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
