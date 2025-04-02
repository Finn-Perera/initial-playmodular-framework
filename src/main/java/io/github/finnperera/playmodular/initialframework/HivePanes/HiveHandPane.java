package io.github.finnperera.playmodular.initialframework.HivePanes;

import io.github.finnperera.playmodular.initialframework.HandClickListener;
import io.github.finnperera.playmodular.initialframework.HiveColour;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;
import io.github.finnperera.playmodular.initialframework.HiveTileType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Map;

// I should be just getting the hand directly

/**
 * Display visualisation of hand, with counts, make clickable
 */
public class HiveHandPane extends VBox {
    private final HashMap<HiveTileType, Integer> tiles;
    private final HiveColour colour;
    private HandClickListener clickListener;

    public HiveHandPane(HivePlayer player) {
        tiles = new HashMap<>();
        colour = player.getColour();
        renderHand(player);
    }

    public void renderHand(HivePlayer player) {
        this.getChildren().clear();
        this.getChildren().add(new Label("Player: " + colour.toString()));
        HBox tilesContainer = new HBox();

        fillTiles(player);

        for (Map.Entry<HiveTileType, Integer> entry : tiles.entrySet()) {
            Color color = getPieceColor(entry.getKey());
            Circle circle = new Circle(20);
            circle.setFill(color);
            // i want to create a hexagon which is the tile type
            VBox vbox = new VBox();
            vbox.getChildren().add(circle);
            vbox.getChildren().add(new Label(entry.getValue().toString()));
            vbox.setOnMouseClicked(mouseEvent -> {
                if (clickListener != null) {
                    if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        // on right click i want to deselect any tile
                        clickListener.onItemClicked(null, null);
                        return;
                    }
                    clickListener.onItemClicked(entry.getKey(), player.getColour());
                }
            });
            tilesContainer.getChildren().add(vbox);
        }
        this.getChildren().add(tilesContainer);
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

    public void setClickListener(HandClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private void fillTiles(HivePlayer player) {
        tiles.clear();
        for (HiveTileType type : HiveTileType.values()) {
            int count = player.getTypeRemainingTiles(type);
            tiles.put(type, count);
        }
    }

    public HiveColour getColour() {
        return colour;
    }
}
