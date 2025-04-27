package io.github.finnperera.playmodular.initialframework;

import io.github.finnperera.playmodular.initialframework.HiveHeuristics.BasicHeuristic;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HeuristicSettingsDialog extends Stage {
    private final Consumer<BasicHeuristic> heuristicConsumer;

    public HeuristicSettingsDialog(Stage parentStage, Consumer<BasicHeuristic> heuristicConsumer) {
        this.heuristicConsumer = heuristicConsumer;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parentStage);

        setTitle("Customise Heuristic");
        BasicHeuristic basicHeuristic = new BasicHeuristic();
        ArrayList<Node> optionControlNodes = new ArrayList<>();
        List<Option<?>> options = basicHeuristic.getOptions();

        for (Option<?> option : options) {
            optionControlNodes.add(OptionFactory.createOptionControl(option));
        }

        Button finaliseButton = new Button("Finalise");
        finaliseButton.setOnAction(e -> {
            basicHeuristic.setOptions(options);
            heuristicConsumer.accept(basicHeuristic);
            close();
        });

        VBox fullPane = new VBox();
        TilePane tilePane = new TilePane();
        tilePane.getChildren().addAll(optionControlNodes);
        fullPane.getChildren().add(tilePane);
        fullPane.getChildren().add(finaliseButton);
        fullPane.setAlignment(Pos.TOP_CENTER);
        setScene(new Scene(fullPane, 600, 200));
    }
}
