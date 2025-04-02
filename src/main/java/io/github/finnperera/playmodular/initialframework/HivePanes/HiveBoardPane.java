package io.github.finnperera.playmodular.initialframework.HivePanes;

import io.github.finnperera.playmodular.initialframework.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.util.List;

/**
 * The actual board pane which is played on
 * Requires:
 * - Hive Game class here
 * - Draw/Render function
 * - SHOULD PASS HANDLING OF CLICKS TO CONTROLLER
 */
public class HiveBoardPane extends Pane {
    private static final double HEX_SIZE = 30;
    double offsetX = 150;
    double offsetY = 150;
    double transformX = 0;
    double transformY = 0;

    boolean debugMode = false;
    boolean controlPressed = false;
    double debugX = 0;
    double debugY = 0;
    Label debugHexCoords;

    private HiveBoardState hiveGame;
    private Group boardContent;
    private TileClickListener clickListener;

    private double lastMouseX, lastMouseY;

    public HiveBoardPane(HiveBoardState hiveGame) {
        this.hiveGame = hiveGame;
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        initialiseUI();
        renderBoard();
    }

    public HiveBoardPane(HiveBoardState hiveGame, double transformX, double transformY) {
        this.hiveGame = hiveGame;
        this.transformX = transformX;
        this.transformY = transformY;
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        initialiseUI();
        renderBoard();
    }

    private void initialiseUI() {
        this.setOnMouseClicked(this::handleMouseClicked);
        this.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });
        this.setOnMouseDragged(drag -> handleMouseDragged(drag, boardContent));
        this.setFocusTraversable(true);
        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.CONTROL) {
                controlPressed = true;
            }
            if (controlPressed && event.getCode() == KeyCode.D) {
                debugMode = true;
            }
        });
        this.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.CONTROL) {
                controlPressed = false;
            }
        });

        this.setOnMouseMoved(mouseEvent -> {
            debugX = mouseEvent.getX();
            debugY = mouseEvent.getY();
            if (debugMode) {
                int[] hexCoords = calculateHexCoordinates(debugX, debugY, HEX_SIZE, offsetX, offsetY);
                debugHexCoords = new Label("q: " + hexCoords[0] + ", r: " + hexCoords[1] + ", s: " + (-hexCoords[0] - hexCoords[1]));
                debugHexCoords.setId("debugHexCoords");
                debugHexCoords.setLayoutX(debugX + 10);
                debugHexCoords.setLayoutY(debugY + 10);
                this.getChildren().removeIf(node -> "debugHexCoords".equals(node.getId()));
                this.getChildren().add(debugHexCoords);
            }
        });
    }

    private void renderBoard() {
        this.getChildren().clear();
        boardContent = new Group();

        fillBoardContent();
        this.getChildren().add(boardContent);
        boardContent.setTranslateX(transformX);
        boardContent.setTranslateY(transformY);
    }

    private void fillBoardContent() {
        for (Hex position : hiveGame.getAllPositions()) {
            HiveTile piece = hiveGame.getPieceAt(position);
            double[] pixelCoordinates = calculatePixelCoordinates(position.getQ(), position.getR(), HEX_SIZE, offsetX, offsetY);

            Group createdPiece = createTile(pixelCoordinates[0], pixelCoordinates[1], piece);
            boardContent.getChildren().add(createdPiece);
        }
    }

    public void updateGame(HiveGame game) {
        hiveGame = game.getBoardState();
        renderBoard();
    }

    private void handleMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            // on right click i want to deselect any tile
            clickListener.onTileClicked(null);
            return;
        }

        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();

        int[] hexCoordinates= calculateHexCoordinates(mouseX,mouseY,HEX_SIZE, offsetX, offsetY);
        Hex hex = new Hex(hexCoordinates[0], hexCoordinates[1], -hexCoordinates[0] -hexCoordinates[1]);
        clickListener.onTileClicked(hex);
    }

    private void handleMouseDragged(MouseEvent mouseEvent, Group boardContent) {
        double changeX = mouseEvent.getX() - lastMouseX;
        double changeY = mouseEvent.getY() - lastMouseY;

        transformX += changeX;
        transformY += changeY;

        boardContent.setTranslateX(transformX);
        boardContent.setTranslateY(transformY);

        Pane highlightLayer = (Pane) getHighlightLayer();
        if (highlightLayer != null) {
            highlightLayer.setTranslateX(transformX);
            highlightLayer.setTranslateY(transformY);
        }

        lastMouseX = mouseEvent.getX();
        lastMouseY = mouseEvent.getY();
    }

    public void highlightPossibleMoves(List<Hex> hexList) {
        if (hexList == null || hexList.isEmpty()) {
            removeHighlightLayer();
            return;
        }

        Pane highlightLayer = (Pane) getHighlightLayer();
        if (highlightLayer != null) {
            this.getChildren().remove(highlightLayer);
        }

        Pane newHighlightLayer = new Pane();

        for (Hex hex : hexList) {
            double[] pixelCoordinates = calculatePixelCoordinates(hex.getQ(), hex.getR(), HEX_SIZE, offsetX, offsetY);
            Polygon highlightHex = createHexagon(pixelCoordinates[0], pixelCoordinates[1], HEX_SIZE);
            highlightHex.setStroke(Color.GREEN);
            highlightHex.setFill(Color.TRANSPARENT);
            newHighlightLayer.getChildren().add(highlightHex);
        }

        newHighlightLayer.setTranslateX(transformX);
        newHighlightLayer.setTranslateY(transformY);

        newHighlightLayer.setId("highlightLayer");
        this.getChildren().add(newHighlightLayer);
    }

    private Node getHighlightLayer() {
        return this.getChildren().stream()
                .filter(node -> "highlightLayer".equals(node.getId()))
                .findFirst()
                .orElse(null);
    }

    public void removeHighlightLayer() {
        Pane highlightLayer = (Pane) getHighlightLayer();
        if (highlightLayer != null) {
            this.getChildren().remove(highlightLayer);
        }
    }

    private Polygon createHexagon(double x, double y, double size) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            double xOffset = size * Math.cos(angle);
            double yOffset = size * Math.sin(angle);
            hex.getPoints().addAll(x + xOffset, y + yOffset);
        }
        return hex;
    }

    private Group createTile(double x, double y, HiveTile tile) {
        Polygon hex = createHexagon(x, y, HEX_SIZE);
        Circle centralCircle = new Circle(x, y, HEX_SIZE / 3);
        hex.setFill(Color.valueOf(tile.getColour().toString()));
        centralCircle.setFill(getTileColour(tile.getTileType()));
        return new Group(hex, centralCircle);
    }

    private Color getTileColour(HiveTileType type) {
        Color color = null;
        switch (type) {
            case QUEEN_BEE -> color = Color.YELLOW;
            case GRASSHOPPER -> color = Color.GREEN;
            case BEETLE -> color = Color.PURPLE;
            case ANT -> color = Color.BLUE;
            case SPIDER -> color = Color.BROWN;
        }
        return color;
    }

    private int[] calculateHexCoordinates(double x, double y, double hexSize, double offsetX, double offsetY) {
        double adjustedX = x - offsetX - transformX;
        double adjustedY = y - offsetY - transformY;

        double q = (adjustedX * Math.sqrt(3) / 3 - adjustedY / 3) / hexSize;
        double r = (adjustedY * 2 / 3) / hexSize;

        int roundedQ = (int) Math.round(q);
        int roundedR = (int) Math.round(r);
        int roundedS = -roundedQ - roundedR;

        double qDiff = Math.abs(roundedQ - q);
        double rDiff = Math.abs(roundedR - r);
        double sDiff = Math.abs(roundedS - (-q - r));

        if (qDiff > rDiff && qDiff > sDiff) {
            roundedQ = -roundedR - roundedS;
        } else if (rDiff > sDiff) {
            roundedR = -roundedQ - roundedS;
        }

        return new int[]{roundedQ, roundedR};
    }

    public double[] calculatePixelCoordinates(int q, int r, double hexSize, double offsetX, double offsetY) {
        double x = offsetX + hexSize * Math.sqrt(3) * (q + r / 2.0);
        double y = offsetY + hexSize * 1.5 * r;
        return new double[]{x, y};
    }

    public void setClickListener(TileClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public double getTransformX() {
        return transformX;
    }

    public double getTransformY() {
        return transformY;
    }
}
