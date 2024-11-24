module io.github.finnperera.playmodular.initialframework {
    requires javafx.controls;
    requires javafx.fxml;


    opens io.github.finnperera.playmodular.initialframework to javafx.fxml;
    exports io.github.finnperera.playmodular.initialframework;
}