package io.github.finnperera.playmodular.initialframework;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class HiveGameDesigner extends Application {
    private final HivePlayer player1 = new HivePlayer(HiveColour.WHITE);
    private final HivePlayer player2 = new HivePlayer(HiveColour.BLACK);
    double hexSize = 30;
    double hexWidth = Math.sqrt(3) * hexSize;
    double hexHeight = 2 * hexSize;
    double offsetX;
    double offsetY;
    Button selectModeButton = new Button("Select Mode");
    private MapBasedStorage<Hex, HiveTile> storage = new MapBasedStorage<>();

    private boolean selectTileMode = false;
    private Hex selectedHex = null;
    private List<Node> selectedHexNodes = new ArrayList<>();

    private HiveTileType curTileType = null;
    private HiveColour curColour = null;
    private Polygon previewHex;
    private HiveRuleEngine ruleEngine = new HiveRuleEngine();
    private Stack<Hex> undoStack = new Stack<>();

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = new Pane();
        Scene scene = new Scene(root, 1280, 640);
        stage.setTitle("Hive Game Creator");
        stage.setScene(scene);
        stage.show();

        HBox sectionContainer = new HBox();
        VBox buttonContainer = new VBox();
        Pane mainPane = new Pane();
        VBox testContainer = new VBox();

        root.getChildren().add(sectionContainer);

        // buttons for affecting main pane
        createButtons(buttonContainer, mainPane);
        buttonContainer.setSpacing(10);
        sectionContainer.getChildren().add(buttonContainer);
        buttonContainer.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));

        // main pane where you can visualise game
        mainPane.setPrefWidth(840);
        mainPane.setPrefHeight(420);
        sectionContainer.getChildren().add(mainPane);
        mainPane.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, null, null)));
        setUpMainPane(mainPane);

        // offsets for calculating pixel coords
        offsetX = mainPane.getWidth() / 2;
        offsetY = mainPane.getHeight() / 2;

        // test container is to allow filling hands and actually getting moves
        sectionContainer.getChildren().add(testContainer);
        setUpTestPane(testContainer, mainPane);
    }

    public void createButtons(Pane container, Pane mainPane) {
        container.getChildren().add(new Label("Tile Type"));

        for (HiveTileType type : HiveTileType.values()) {
            Button button = new Button();
            button.setText(type.toString());
            button.setOnAction(event -> {
                curTileType = type;
                turnOffSelectedMode(mainPane);
            });
            container.getChildren().add(button);
        }

        container.getChildren().add(new Label("Colours"));

        for (HiveColour colour : HiveColour.values()) {
            Button button = new Button();
            button.setText(colour.toString());
            button.setOnAction(event -> {
                curColour = colour;
                turnOffSelectedMode(mainPane);
            });
            container.getChildren().add(button);
        }

        container.getChildren().add(new Label("Select Or Place: "));
        selectModeButton.setOnMouseClicked(event -> {
            if (selectTileMode) {
                turnOffSelectedMode(mainPane);
            } else {
                turnOnSelectedMode();
            }
        });

        container.getChildren().add(selectModeButton);

        container.getChildren().add(new Label("Undo:"));
        Button undoButton = new Button("Undo");
        selectModeButton.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        undoButton.setOnMouseClicked(event -> {
            if (!undoStack.isEmpty()) {
                Hex toUndo = undoStack.pop();
                removeHexAt(toUndo, mainPane);
            }
        });

        container.getChildren().add(undoButton);
    }

    public void setUpMainPane(Pane container) {
        //createPreviewHex(hexSize, container);

        container.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            int[] hexCoordinates = calculateHexCoordinates(mouseX, mouseY, hexSize, offsetX, offsetY);
            Hex hex = new Hex(hexCoordinates[0], hexCoordinates[1], -hexCoordinates[0] - hexCoordinates[1]);
            if (selectTileMode) {
                if (selectedHex != null) {
                    toggleOutlineOnTile(selectedHex, container);
                    clearHighlightedMoves(container);
                }
                selectedHex = hex;
                toggleOutlineOnTile(selectedHex, container);
            } else {
                undoStack.add(hex);
                placeHexAt(hexCoordinates[0], hexCoordinates[1], hexSize, container);
            }
        });

        container.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            int[] hexCoordinates = calculateHexCoordinates(mouseX, mouseY, hexSize, offsetX, offsetY);

            //updatePreviewHex(hexCoordinates[0], hexCoordinates[1]);
        });
    }

    public void setUpTestPane(Pane container, Pane main) {
        container.getChildren().add(new Label("Player 1 Hand:"));
        createHandForPlayer(container, player1);
        container.getChildren().add(new Label("Player 2 Hand:"));
        createHandForPlayer(container, player2);

        container.getChildren().add(new Label("Show selected piece moves: "));
        Button toggleMovesShowing = new Button("Show Moves");

        toggleMovesShowing.setOnAction(event -> {
            toggleMovesShowing(main);
        });

        container.getChildren().add(toggleMovesShowing);
    }

    private void toggleMovesShowing(Pane main) {
        clearHighlightedMoves(main);
        if (selectedHex != null) {
            HiveTile piece = storage.getPieceAt(selectedHex);
            List<HiveMove> moves = ruleEngine.generatePieceMoves(new HiveBoardState(storage), piece);
            for (HiveMove move : moves) {
                highlightValidHexMove(move.getNextPosition(), main);
            }
        }
    }

    private void toggleOutlineOnTile(Hex hex, Pane container) {
        Node tile = getTileNodeAt(hex, container);
        if (tile instanceof Group hexTile) {
            if (hexTile.getChildren().getFirst() instanceof Polygon hexagon) {
                if (hexagon.getStroke() != Color.RED) {
                    hexagon.setStroke(Color.RED);
                    hexagon.setStrokeWidth(3);
                } else {
                    hexagon.setStroke(null);
                    hexagon.setStrokeWidth(1);
                }
            }
            hexTile.toFront();
        }
    }

    private void highlightValidHexMove(Hex hex, Pane container) {
        double[] pixelCoords = calculatePixelCoordinates(hex.getQ(), hex.getR(), hexSize, offsetX, offsetY);
        double centerX = pixelCoords[0];
        double centerY = pixelCoords[1];

        Polygon hexagon = createHexagon(centerX, centerY, hexSize);
        hexagon.setFill(Color.TRANSPARENT);
        hexagon.setStroke(Color.GREEN);
        hexagon.setStrokeWidth(3);
        hexagon.toFront();
        selectedHexNodes.add(hexagon);
        container.getChildren().add(hexagon);
    }

    private void turnOnSelectedMode() {
        selectTileMode = true;
        selectedHex = null;

        selectModeButton.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
    }

    private void turnOffSelectedMode(Pane main) {
        selectTileMode = false;

        if (selectedHex != null) {
            toggleOutlineOnTile(selectedHex, main);
        }

        if (!selectedHexNodes.isEmpty()) {
            clearHighlightedMoves(main);
        }

        selectedHex = null;
        selectModeButton.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
    }

    private void clearHighlightedMoves(Pane main) {
        for (Node node : selectedHexNodes) {
            main.getChildren().remove(node);
        }
        selectedHexNodes.clear();
    }

    private void createHandForPlayer(Pane container, HivePlayer player) {
        VBox spinnerContainer = new VBox();
        Spinner<Integer> bee = createPieceSpinner(spinnerContainer, "Queen Bee", 1);
        Spinner<Integer> grasshopper = createPieceSpinner(spinnerContainer, "Grasshopper", 2);
        Spinner<Integer> beetle = createPieceSpinner(spinnerContainer, "Beetle", 2);
        Spinner<Integer> ant = createPieceSpinner(spinnerContainer, "Soldier Ant", 3);
        Spinner<Integer> spider = createPieceSpinner(spinnerContainer, "Spider", 3);
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> {
            HashMap<HiveTileType, Integer> hand = new HashMap<>();
            hand.put(HiveTileType.QUEEN_BEE, bee.getValue());
            hand.put(HiveTileType.GRASSHOPPER, grasshopper.getValue());
            hand.put(HiveTileType.BEETLE, beetle.getValue());
            hand.put(HiveTileType.ANT, ant.getValue());
            hand.put(HiveTileType.SPIDER, spider.getValue());
            player.setHand(hand);
        });
        container.getChildren().add(spinnerContainer);
        container.getChildren().add(submitButton);
    }

    private Spinner<Integer> createPieceSpinner(Pane container, String pieceName, int normal) {
        container.getChildren().add(new Label(pieceName + ": "));
        Spinner<Integer> piece = new Spinner<>(0, 10, normal);
        piece.setId(pieceName);
        container.getChildren().add(piece);
        return piece;
    }

    private void removeHexAt(Hex hex, Pane container) {
        String id = hex.getQ() + " " + hex.getR();
        container.getChildren().removeIf(node -> id.equals(node.getId()));
        storage.removePieceAt(hex);
    }

    private Node getTileNodeAt(Hex hex, Pane container) {
        String id = hex.getQ() + " " + hex.getR();
        return container.getChildren()
                .stream()
                .filter(node -> id.equals(node.getId()))
                .findFirst()
                .orElse(null);
    }

    private void placeHexAt(int q, int r, double hexSize, Pane container) {
        if (curColour == null || curTileType == null) {
            System.out.println("Select Colour and Tile Type");
            return;
        }
        double[] pixelCoords = calculatePixelCoordinates(q, r, hexSize, offsetX, offsetY);
        double centerX = pixelCoords[0];
        double centerY = pixelCoords[1];

        Hex position = new Hex(q, r, -q - r);
        if (storage.hasPieceAt(position)) {
            storage.removePieceAt(position);
            removeHexAt(position, container);
        }

        Group hex = createHexagon(centerX, centerY, hexSize, getPieceColor(curTileType));
        Polygon hexagon = (Polygon) hex.getChildren().getFirst();
        hex.setId(q + " " + r);
        hexagon.setFill(Color.valueOf(curColour.toString()));
        hexagon.setStrokeWidth(1);

        container.getChildren().add(hex);


        storage.placePieceAt(position, new HiveTile(curTileType, position, curColour));
    }

    private void updatePreviewHex(int q, int r) {
        double[] hexCenter = calculatePixelCoordinates(q, r, hexSize, offsetX, offsetY);
        double centerX = hexCenter[0];
        double centerY = hexCenter[1];

        previewHex.setTranslateX(centerX - previewHex.getLayoutBounds().getWidth() / 2);
        previewHex.setTranslateY(centerY - previewHex.getLayoutBounds().getHeight() / 2);
    }

    private void createPreviewHex(double hexSize, Pane container) {
        previewHex = createHexagon(0, 0, hexSize);
        previewHex.setFill(Color.TRANSPARENT);
        previewHex.setStroke(Color.RED);
        previewHex.setOpacity(0.5);
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

    private Group createHexagon(double x, double y, double size, Color circleColour) {
        Polygon hex = createHexagon(x, y, size);
        Circle centralCircle = new Circle(x, y, size / 3);
        centralCircle.setFill(circleColour);
        return new Group(hex, centralCircle);
    }

    private Color getPieceColor(HiveTileType type) {
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
        double adjustedX = x - offsetX;
        double adjustedY = y - offsetY;

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

    private double calculateHexX(int q, int r, double hexWidth, double offsetX) {
        return offsetX + (q + r / 2.0) * hexWidth;
    }

    private double calculateHexY(int r, double hexHeight, double offsetY) {
        return offsetY + r * (hexHeight * 0.75);
    }

    public double[] calculatePixelCoordinates(int q, int r, double hexSize, double offsetX, double offsetY) {
        double x = offsetX + hexSize * Math.sqrt(3) * (q + r / 2.0);
        double y = offsetY + hexSize * 1.5 * r;
        return new double[]{x, y};
    }

}
