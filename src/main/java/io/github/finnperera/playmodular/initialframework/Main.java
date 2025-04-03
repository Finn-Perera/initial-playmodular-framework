package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.AlphaBetaMinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.MinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch.MonteCarloModel;
import io.github.finnperera.playmodular.initialframework.HiveHeuristics.BasicHeuristic;
import io.github.finnperera.playmodular.initialframework.HivePanes.HiveGamePane;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/*
    What I need here:
    - Menu of games to play (can come later)
    - I need a game instance to be created
    - A visualiser should create a pane which represents the game board
     */
public class Main extends Application {

    AtomicReference<HivePlayer> player1Selection = new AtomicReference<>(null);
    AtomicReference<HivePlayer> player2Selection = new AtomicReference<>(null);

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
                            onGameButtonClicked(stage, hiveGame);
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
        ChoiceBox<String> opponentChoiceBox = new ChoiceBox<>();
        opponentChoiceBox.getItems().addAll("Human", "Monte Carlo", "Minimax", "Alpha-Beta");
        opponentChoiceBox.setValue("Human");

        opponentChoiceBox.setOnAction(event -> {
            String selectedAI = opponentChoiceBox.getValue();
            HiveAI updatedPlayer = null;

            switch (opponentChoiceBox.getValue()) {
                case "Monte Carlo" -> updatedPlayer = new HiveAI(colour,
                        new MonteCarloModel<>(50));
                case "Minimax" -> {
                    updatedPlayer = new HiveAI(colour, null);
                    updatedPlayer.setModel(new MinimaxModel<>(updatedPlayer, new BasicHeuristic()));
                }
                case "Alpha-Beta" -> {
                    updatedPlayer = new HiveAI(colour, null);
                    updatedPlayer.setModel(new AlphaBetaMinimaxModel<>(updatedPlayer, new BasicHeuristic()));
                }
                case null, default -> updatedPlayer = null;
            }

            player.set(updatedPlayer);
        });

        root.getChildren().add(opponentChoiceBox);
    }


    private void createGameButton(Stage stage, Pane root) {
        Button createGameButton = new Button("Create Game");
        createGameButton.setOnAction(event -> {
            HiveGame game;
            HivePlayer player1 = player1Selection.get() == null ? new HivePlayer(HiveColour.WHITE) : player1Selection.get();
            HivePlayer player2 = player2Selection.get() == null ? new HivePlayer(HiveColour.BLACK) : player2Selection.get();
            game = new HiveGame(new HiveRuleEngine(), player1, player2, new HiveBoardState());
            onGameButtonClicked(stage, game);
        });
        root.getChildren().add(createGameButton);
    }

    private void onGameButtonClicked(Stage stage, HiveGame hiveGame) {
        HiveGame game;
        game = hiveGame;
        HiveGamePane gamePane = new HiveGamePane(game);
        HiveBoardGameController controller = new HiveBoardGameController(gamePane, game);
        Scene gameScene = new Scene(gamePane, 1280, 640);
        stage.setScene(gameScene);
    }
}
