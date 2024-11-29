package io.github.finnperera.playmodular.initialframework;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class HelloApplication extends Application implements HexMapVisualiser.MoveListener {

    HiveGame game;
    HexMapVisualiser visualiser;

    @Override
    public void start(Stage stage) throws IOException {
        Pane root = new Pane(); // Pane for graphical visualization
        Scene scene = new Scene(root, 1280, 640);
        stage.setTitle("Hive Game");
        stage.setScene(scene);
        stage.show();

        Pane pane = new Pane();
        pane.setLayoutX(500);
        pane.setLayoutY(250);
        root.getChildren().add(pane);
        runRandomHiveGame(pane, 20);
    }
    /*
    public void runChoiceHiveGame(Pane root, int maxTurns) {

        Scanner scanner = new Scanner(System.in);

        HiveRuleEngine ruleEngine = new HiveRuleEngine();
        HivePlayer player1 = new HivePlayer(HiveColour.WHITE);
        HivePlayer player2 = new HivePlayer(HiveColour.BLACK);
        HiveGame hiveGame = new HiveGame(ruleEngine, player1, player2, new HiveBoardState());
        HexMapVisualiser visualiser = new HexMapVisualiser(root); // Pass the Pane to the visualiser

        int turnCount = 0;
        Random random = new Random();
        boolean completedTurn = false;
        while (!hiveGame.isTerminalState(hiveGame.getBoardState()) && turnCount <= maxTurns) {
            completedTurn = false;
            visualiser.refresh(hiveGame);
            List<HiveMove> possibleMoves = hiveGame.getAvailableMoves(hiveGame.getCurrentPlayer());
            System.out.println("Possible moves: " );
            if (!possibleMoves.isEmpty()) {
                List<HiveMove> choiceMoves = new ArrayList<>();
                for (int i = 0; i < 5 && i < possibleMoves.size(); i++) {
                    choiceMoves.add(possibleMoves.get(random.nextInt(possibleMoves.size() - 1)));
                }
            }
            turnCount++;
            System.out.println("Turn" + turnCount);
        }
        visualiser.refresh(hiveGame);
        scanner.close();
    }
       */
    public void runRandomHiveGame(Pane root, int maxTurns) {
        HiveRuleEngine ruleEngine = new HiveRuleEngine();
        HivePlayer player1 = new HivePlayer(HiveColour.WHITE);
        HivePlayer player2 = new HivePlayer(HiveColour.BLACK);
        final HiveGame[] hiveGame = {new HiveGame(ruleEngine, player1, player2, new HiveBoardState())};
        HexMapVisualiser hmp = new HexMapVisualiser(root);
        int turnCount = 0;
        Random random = new Random();
        long totalTime = System.currentTimeMillis();
        Task<Void> gameTask = new Task<>() {
            int turnCount = 0;

            @Override
            protected Void call() throws Exception {
                while (!hiveGame[0].isTerminalState(hiveGame[0].getBoardState()) && turnCount <= maxTurns) {
                    long startTime = System.currentTimeMillis();
                    List<HiveMove> possibleMoves = hiveGame[0].getAvailableMoves(hiveGame[0].getCurrentPlayer());
                    long endTime = System.currentTimeMillis();
                    System.out.println("Moves Take " + (endTime - startTime) + " milliseconds");

                    if (!possibleMoves.isEmpty()) {
                        BoardState<Hex, HiveTile> newBoard = hiveGame[0].makeMove(
                                hiveGame[0].getBoardState(),
                                possibleMoves.get(random.nextInt(possibleMoves.size()))
                        );
                        hiveGame[0] = new HiveGame(ruleEngine, player1, player2, (HiveBoardState) newBoard, hiveGame[0].getTurn());
                    }

                    // Update the game state and UI
                    Platform.runLater(() -> {
                        hiveGame[0].nextTurn();
                        hmp.refresh(hiveGame[0]);
                    });

                    turnCount++;
                    System.out.println("Turn " + turnCount);

                    // Simulate delay for observing each step
                    Thread.sleep(1000); // 1-second delay between turns
                }

                // Log final statistics after the loop ends
                long finalTime = System.currentTimeMillis();
                System.out.println("Total Time Taken: " + (finalTime - totalTime) + " milliseconds");

                // Final UI update
                Platform.runLater(() -> hmp.refresh(hiveGame[0]));

                return null;
            }
        };

        // Handle exceptions, if any
        gameTask.setOnFailed(event -> {
            Throwable exception = gameTask.getException();
            System.err.println("Error in gameTask: " + exception.getMessage());
            exception.printStackTrace();
        });

        // Run the task in a separate thread
        Thread gameThread = new Thread(gameTask);
        gameThread.setDaemon(true); // Ensure the thread exits when the application closes
        gameThread.start();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void onMoveChosen(HiveMove move) {

    }
}