package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MenuController {

    @FXML
    StackPane stack;

    @FXML
    Button startButton;

    @FXML
    public void initialize() {
        stack.setStyle("-fx-background-color: gray");
        startButton.setStyle("-fx-border-color: white; -fx-background-color: white; -fx-border-radius: 40px;");
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: LIGHTGRAY"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: white"));
    }

    @FXML
    private void startGame(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/cz/spse/battleships/setup.fxml")));
        stage.setScene(new Scene(root));
        stage.show();
    }
}
