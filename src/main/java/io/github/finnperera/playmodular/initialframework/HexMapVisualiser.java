package io.github.finnperera.playmodular.initialframework;

import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;

import java.util.Map;
import java.util.List;


public class HexMapVisualiser {

    private final Pane root;

    public HexMapVisualiser(Pane root) {
        this.root = root;
    }

    public void refresh(HiveGame hiveGame) {
        root.getChildren().clear();
        drawHexMap(hiveGame);
    }

    public interface MoveListener{

        void onMoveChosen(HiveMove move);
    }

    public void drawHexMap(HiveGame hiveGame) {
        MapBasedStorage<Hex, HiveTile> storage = hiveGame.getBoardState().getBoard();

        int minQ = storage.getAllPositions().stream().mapToInt(Hex::getQ).min().orElse(0);
        int maxQ = storage.getAllPositions().stream().mapToInt(Hex::getQ).max().orElse(0);
        int minR = storage.getAllPositions().stream().mapToInt(Hex::getR).min().orElse(0);
        int maxR = storage.getAllPositions().stream().mapToInt(Hex::getR).max().orElse(0);

        double hexSize = 60; // Size of the hexagon
        double hexWidth = Math.sqrt(3) * hexSize; // Width of a hexagon
        double hexHeight = 2 * hexSize; // Height of a hexagon
        double offsetX = 50; // Offset for x positioning
        double offsetY = 50; // Offset for y positioning

        for (int r = minR; r <= maxR; r++) {
            for (int q = minQ; q <= maxQ; q++) {
                Hex key = new Hex(q, r, -q - r);
                HiveTile tile = storage.getPieceAt(key);

                double x = offsetX + (q + r / 2.0) * hexWidth;
                double y = offsetY + r * (hexHeight * 0.75); // 0.75 accounts for staggered rows

                Color circleColor = Color.LIGHTGRAY;  // Default color for the central circle

                if (tile != null) {
                    circleColor = switch (tile.getTileType()) {
                        case QUEEN_BEE -> Color.YELLOW;
                        case GRASSHOPPER -> Color.GREEN;
                        case ANT -> Color.BLUE;
                        case BEETLE -> Color.PURPLE;
                        case SPIDER -> Color.BROWN;
                    };
                }
                Group hexWithCircle = createHexagon(x, y, hexSize, circleColor);
                Polygon hexagon = (Polygon) hexWithCircle.getChildren().getFirst();
                Circle circle = (Circle) hexWithCircle.getChildren().getLast();

                hexagon.setStroke(Color.DARKGRAY);
                if (tile != null) {
                    hexagon.setFill(Color.valueOf(tile.getColour().toString()));
                    Text tileLabel = new Text(x - 10, y + 5, tile.getTileType().toString().substring(0, 1));
                    tileLabel.autosize();
                    if ((tile.getColour() == HiveColour.BLACK)) {
                        tileLabel.setFill(Color.WHITE);
                    }
                    root.getChildren().add(tileLabel);
                } else {
                    hexagon.setFill(Color.LIGHTGRAY);
                }

                root.getChildren().add(hexWithCircle);
            }
        }
        // Display player hands
        double playerX = offsetX + (maxQ - minQ + 1) * hexWidth + 20; // Display to the right of the map
        double playerY = offsetY;

        for (HivePlayer player : hiveGame.getPlayers()) {
            Text playerHeader = new Text(playerX, playerY, player.getColour() + " Player's Hand:");
            root.getChildren().add(playerHeader);
            playerY += 20;

            for (Map.Entry<HiveTileType, Integer> entry : player.getTiles().entrySet()) {
                Text tileText = new Text(playerX, playerY, entry.getKey() + ": " + entry.getValue());
                root.getChildren().add(tileText);
                playerY += 20;
            }
            playerY += 20; // Add spacing between players
        }
    }

    public void createChoiceButtons(List<HiveMove> possibleMoves, HiveGame game, MoveListener listener) {
        HBox buttonBox = new HBox(10);  // Horizontal layout to hold the buttons
        buttonBox.setLayoutX(50);  // Set the starting X position for buttons
        buttonBox.setLayoutY(500); // Set the starting Y position for buttons

        int numChoices = possibleMoves.size();

        for (int i = 0; i < numChoices; i++) {
            Button moveButton = new Button("Move " +  i);
            final int moveIndex = i; // To refer to the correct move in the button click handler

            // Set the action for button click
            moveButton.setOnAction(event -> {
                listener.onMoveChosen(possibleMoves.get(moveIndex));
            });

            buttonBox.getChildren().add(moveButton); // Add button to the layout
        }

        // Add the buttons to the root pane
        root.getChildren().add(buttonBox);
    }

    private Group createHexagon(double x, double y, double size, Color circleColour) {
        Polygon hex = createHexagon(x, y, size);
        Circle centralCircle = new Circle(x, y, size / 3);
        centralCircle.setFill(circleColour);
        return new Group(hex, centralCircle);
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

    public static void printHexMap(HiveGame game) {
        MapBasedStorage<Hex, HiveTile> storage = game.getBoardState().getBoard();

        int minQ = storage.getAllPositions().stream().mapToInt(Hex::getQ).min().orElse(0);
        int maxQ = storage.getAllPositions().stream().mapToInt(Hex::getQ).max().orElse(0);
        int minR = storage.getAllPositions().stream().mapToInt(Hex::getR).min().orElse(0);
        int maxR = storage.getAllPositions().stream().mapToInt(Hex::getR).max().orElse(0);

        for (HivePlayer player : game.getPlayers()) {
            System.out.println(player.getColour() + " Player's Hand: ");
            for (Map.Entry<HiveTileType, Integer> entry : player.getTiles().entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
            System.out.println();
        }

        // Iterate through rows (R coordinates)
        for (int r = minR; r <= maxR; r++) {
            StringBuilder line = new StringBuilder();

            // Add horizontal offset for odd rows
            if (r % 2 != 0) {
                line.append("    ");
            }

            // Iterate through columns (Q coordinates)
            for (int q = minQ; q <= maxQ; q++) {
                // Correctly calculate the hex key
                Hex key = new Hex(q, r, -q - r);
                HiveTile tile = storage.getPieceAt(key);

                if (tile != null) {
                    // Compact representation with color and first letter of tile type
                    String value = tile.getColour().toString().charAt(0) +
                            tile.getTileType().toString().substring(0, 1);
                    line.append("[").append(value).append("] ");
                } else {
                    line.append("    "); // Consistent spacing for empty hexes
                }
            }


            System.out.println();
            System.out.println(line);
        }
    }
}
