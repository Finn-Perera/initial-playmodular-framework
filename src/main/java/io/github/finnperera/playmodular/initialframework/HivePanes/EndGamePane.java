package io.github.finnperera.playmodular.initialframework.HivePanes;

import io.github.finnperera.playmodular.initialframework.GameResult;
import io.github.finnperera.playmodular.initialframework.HiveColour;
import io.github.finnperera.playmodular.initialframework.HiveGame;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class EndGamePane extends Pane {
    private HiveGame game;
    private HiveColour winningColour;
    private Label resultLabel;
    private Runnable onMainMenuClicked;
    private Runnable onNextGameClicked;
    private boolean showNextGame = false;

    public EndGamePane(HiveGame game) {
        this.game = game;
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

        if (showNextGame) {
            container.getChildren().add(nextGameButton());
        }

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

    public void show(HiveGame game) {
        this.game = game;
        initialiseUI();
        setResultLabel();
        this.setVisible(true);
    }

    private Button nextGameButton() {
        Button nextButton = new Button("Next Game");
        nextButton.setOnMouseClicked(event -> {
            if (onNextGameClicked != null) {
                onNextGameClicked.run();
            }
        });
        return nextButton;
    }

    private Button viewBoardButton() {
        Button viewBoardButton = new Button("View Board");
        viewBoardButton.setOnMouseClicked(event -> hideUI());
        return viewBoardButton;
    }

    private Button returnToMenuButton() {
        Button returnButton = new Button("Return to Main Menu");
        returnButton.setOnMouseClicked(event -> mainMenuClicked());
        return returnButton;
    }

    private void mainMenuClicked() {
        if (onMainMenuClicked != null) {
            onMainMenuClicked.run();
        }
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

    public void setOnMainMenuClicked(Runnable callback) {
        this.onMainMenuClicked = callback;
    }

    public void setOnNextGameClicked(Runnable callback) {
        this.onNextGameClicked = callback;
        this.showNextGame = true;
    }
}
