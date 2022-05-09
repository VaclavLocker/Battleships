package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;


public class GameboardController implements Initializable {

    @FXML
    Pane yourPane;

    @FXML
    Pane enemyPane;

    @FXML
    Label turn;

    @FXML
    Button sendMsg;

    @FXML
    TextField textField;

    @FXML
    TextArea textArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SetupController.type == SetupController.SERVER) {
            new GameBoardServer(yourPane, enemyPane, turn, sendMsg, textField, textArea).initialize();
        } else {
            new GameBoardClient(yourPane, enemyPane, turn, sendMsg, textField, textArea).initialize();
        }
    }
}

