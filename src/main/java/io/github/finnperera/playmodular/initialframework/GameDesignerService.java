package io.github.finnperera.playmodular.initialframework;

import javafx.scene.Scene;
import javafx.stage.Stage;


import java.util.function.Consumer;

public class GameDesignerService {
    private final Stage primaryStage;

    public GameDesignerService(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // should handle try catch here
    public void openGameDesigner(Scene priorScene, Consumer<HiveGame> onGameStateFinished) throws Exception {
        HiveGameDesigner designer = new HiveGameDesigner(hiveGame -> {
            primaryStage.setScene(priorScene); // swap back to prev (or next) scene
            onGameStateFinished.accept(hiveGame); // pass game state back
        });

        Scene designerScene = new Scene(designer);
        primaryStage.setScene(designerScene);
    }
}
