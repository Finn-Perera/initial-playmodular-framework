module io.github.finnperera.playmodular.initialframework {
    requires javafx.controls;
    requires javafx.fxml;


    opens io.github.finnperera.playmodular.initialframework to javafx.fxml;
    exports io.github.finnperera.playmodular.initialframework;
    exports io.github.finnperera.playmodular.initialframework.HivePanes;
    opens io.github.finnperera.playmodular.initialframework.HivePanes to javafx.fxml;
    exports io.github.finnperera.playmodular.initialframework.HivePlayers;
    opens io.github.finnperera.playmodular.initialframework.HivePlayers to javafx.fxml;
}