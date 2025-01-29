package io.github.finnperera.playmodular.initialframework.HivePanes;

import io.github.finnperera.playmodular.initialframework.GameResult;
import io.github.finnperera.playmodular.initialframework.HiveColour;
import io.github.finnperera.playmodular.initialframework.HiveGame;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Central UI for playing the game Hive:
 * - Needs Display for pieces in hands
 * - Needs Display for board
 * - Should be interacted with to play the game
 */
public class HiveGamePane extends StackPane {
    private HiveBoardPane board;
    private HBox container;
    private HiveGame game;
    private List<HiveHandPane> handPaneList;
    private EndGamePane endGamePane;

    public HiveGamePane(HiveGame game) {
        this.container = new HBox();
        this.game = game;
        handPaneList = new ArrayList<>();
        handPaneList.add(new HiveHandPane(game.getPlayers().getFirst()));
        handPaneList.add(new HiveHandPane(game.getPlayers().getLast()));
        board = new HiveBoardPane(game.getBoardState());
        this.setPrefSize(1280, 1024);
        initialiseUI();
    }

    public void update() {
        this.getChildren().clear();
        this.container = new HBox();
        handPaneList = new ArrayList<>();
        board = new HiveBoardPane(game.getBoardState(), board.getTransformX(), board.getTransformY());
        handPaneList.add(new HiveHandPane(game.getPlayers().getFirst()));
        handPaneList.add(new HiveHandPane(game.getPlayers().getLast()));
        initialiseUI();
    }

    private void initialiseUI() {
        container.getChildren().clear();
        // left side i want hands?
        VBox handSelection = new VBox();
        Label label = new Label("Player Turn: " + game.getCurrentPlayer().getColour().toString());
        handSelection.getChildren().add(label);
        for (HiveHandPane handPane : handPaneList) {
            handSelection.getChildren().add(handPane);
        }

        VBox.setVgrow(handSelection, Priority.ALWAYS);

        container.getChildren().add(handSelection);

        HBox.setHgrow(board, Priority.ALWAYS);
        board.prefWidthProperty().bind(container.widthProperty());
        board.prefHeightProperty().bind(container.heightProperty());
        container.getChildren().add(board);
        this.getChildren().add(container);
        HBox.setHgrow(container, Priority.ALWAYS);

        endGamePane = new EndGamePane(game);
        this.getChildren().add(endGamePane);
    }

    public void showEndGame() {
        endGamePane.setResultLabel();
        endGamePane.setVisible(true);
    }

    public void showEndGame(GameResult result) {
        endGamePane.setResultLabel(result);
        endGamePane.setVisible(true);
    }

    public HiveBoardPane getBoard() {
        return board;
    }

    public List<HiveHandPane> getHandPaneList() {
        return handPaneList;
    }

    public void setGame(HiveGame game) {
        this.game = game;
    }
}
