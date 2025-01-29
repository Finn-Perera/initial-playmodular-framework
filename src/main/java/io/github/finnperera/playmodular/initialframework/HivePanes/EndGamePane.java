package io.github.finnperera.playmodular.initialframework.HivePanes;

import io.github.finnperera.playmodular.initialframework.GameResult;
import io.github.finnperera.playmodular.initialframework.HiveColour;
import io.github.finnperera.playmodular.initialframework.HiveGame;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class EndGamePane extends Pane {
    HiveGame game;
    HiveColour winningColour;
    Label resultLabel;
    Scene returnScene;

    public EndGamePane(HiveGame game) {
        this.game = game;

        initialiseUI();
        this.setVisible(false);
    }

    private void initialiseUI() {
        this.getChildren().clear();
        this.setMouseTransparent(false);

        VBox container = new VBox();
        container.setSpacing(10);
        container.setAlignment(Pos.CENTER);

        container.getChildren().add(resultLabel());
        container.getChildren().add(returnToMenuButton());
        container.getChildren().add(viewBoardButton());

        container.layoutXProperty().bind(this.widthProperty().subtract(container.widthProperty()).divide(2));
        container.layoutYProperty().bind(this.heightProperty().subtract(container.heightProperty()).divide(2));

        this.getChildren().add(container);
    }

    private void hideUI() {
        this.getChildren().clear();
        this.setMouseTransparent(true);

        VBox container = new VBox();

        Button showEndGameButton = new Button("Show End of Game Menu");
        showEndGameButton.setOnMouseClicked(event -> initialiseUI());
        container.getChildren().add(showEndGameButton);

        container.layoutXProperty().bind(this.widthProperty().subtract(container.widthProperty()).divide(2));
        container.layoutYProperty().bind(this.heightProperty().subtract(container.heightProperty()).divide(2));

        container.setMouseTransparent(false);
        showEndGameButton.setMouseTransparent(false);
        this.getChildren().add(container);
    }

    // i want button to view board?
    private Button viewBoardButton() {
        Button viewBoardButton = new Button("View Board");
        viewBoardButton.setOnMouseClicked(event -> hideUI());
        return viewBoardButton;
    }

    // i want button to return to main menu
    private Button returnToMenuButton() {
        Button returnButton = new Button("Return to Main Menu");
        returnButton.setOnMouseClicked(null); // not sure how to return back right now?
        return returnButton;
    }

    private Label resultLabel() {
        resultLabel = new Label("GAME OVER");
        return resultLabel;
    }

    public void setResultLabel() {
        GameResult result = game.getGameResult(game.getCurrentPlayer());
        if (result == null) return; // bug?
        if (result == GameResult.DRAW) {
            resultLabel.setText("DRAW!");
        } else if (result == GameResult.WIN) {
            resultLabel.setText("WINNER: " + game.getCurrentPlayer().getColour().toString());
        } else {
            resultLabel.setText("WINNER: " + game.getCurrentPlayer().getColour().oppositeColour().toString());
        }
    }

    // may need changing
    public void setResultLabel(GameResult result) {
        assert result != null;
        if (result == GameResult.DRAW) {
            resultLabel.setText("DRAW!");
        }
    }

    public void setReturnScene(Scene returnScene) {
        this.returnScene = returnScene;
    }
}
