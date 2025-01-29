package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch.MonteCarloModel;
import io.github.finnperera.playmodular.initialframework.HivePanes.HiveGamePane;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.util.concurrent.CompletableFuture;

/*
    What I need here:
    - Menu of games to play (can come later)
    - I need a game instance to be created
    - A visualiser should create a pane which represents the game board
     */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Pane root = new HBox();
        Scene scene = new Scene(root, 1280, 640);
        stage.setTitle("Hive");
        stage.setScene(scene);

        initialiseGameStateButton(root, scene, stage);
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

    private void createGameButton(Stage stage, Pane root) {
        Button createGameButton = new Button("Create Game");
        createGameButton.setOnAction(event -> {
            HiveAI aiPlayer = new HiveAI(HiveColour.BLACK, new MonteCarloModel<>(50));
            //onGameButtonClicked(stage, new HiveGame(new HiveRuleEngine(), new HivePlayer(HiveColour.WHITE), new HivePlayer(HiveColour.BLACK), new HiveBoardState()));
            onGameButtonClicked(stage, new HiveGame(new HiveRuleEngine(), new HivePlayer(HiveColour.WHITE), aiPlayer, new HiveBoardState()));
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
